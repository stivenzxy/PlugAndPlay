package com.plugAndPlay.Views;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Facade.PluginManager;
import com.plugAndPlay.Factories.AudioSourceFactory;
import com.plugAndPlay.Factories.PluginPanelFactory;
import com.plugAndPlay.Factories.ViewActionFactory;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainView {

    private final JFrame frame;
    private final JTextArea messageArea;

    private final PluginManager pluginManager;
    private final AudioRepository audioRepository;

    private final DefaultListModel<Plugin> pluginListModel;
    private final JList<Plugin> pluginJList;
    private final JPanel pluginDetailPanel;
    private final CardLayout pluginCardLayout;

    private final PluginPanelFactory pluginPanelFactory;

    private final JTree fileTree;
    private final DefaultTreeModel fileTreeModel;
    private final DefaultMutableTreeNode rootNode;
    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";
    private static final String SESSION_FILE_PATH = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public MainView(AudioSourceFactory audioSourceFactory, PluginManager pluginManager, AudioRepository audioRepository) {
        this.pluginManager = pluginManager;
        this.audioRepository = audioRepository;

        this.pluginPanelFactory = new PluginPanelFactory(this::log, this.audioRepository, this::refreshFileTree);

        frame = new JFrame("Plug & Play - MicroKernel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout(5, 5));


        Runnable refresher = this::refreshFileTree;
        ViewActionFactory viewActionFactory = new ViewActionFactory(audioSourceFactory, this::log, refresher);
        Action recordAudioAction = viewActionFactory.createRecordAudioAction();
        Action loadAudioAction = viewActionFactory.createLoadAudioAction(this.frame);

        this.rootNode = new DefaultMutableTreeNode(new File(AUDIO_DIRECTORY));
        this.fileTreeModel = new DefaultTreeModel(rootNode);
        this.fileTree = new JTree(fileTreeModel);

        this.pluginListModel = new DefaultListModel<>();
        this.pluginJList = new JList<>(pluginListModel);

        this.pluginCardLayout = new CardLayout();
        this.pluginDetailPanel = new JPanel(pluginCardLayout);
        pluginDetailPanel.setBorder(new TitledBorder("Paso 3: Acciones del Plugin"));

        JPanel actionPanel = createActionPanel(recordAudioAction, loadAudioAction);
        JSplitPane leftPanel = createLeftPanel();
        JScrollPane logPanel = createLogPanel();

        JSplitPane mainContentPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, pluginDetailPanel);
        mainContentPanel.setDividerLocation(250);

        frame.add(actionPanel, BorderLayout.NORTH);
        frame.add(mainContentPanel, BorderLayout.CENTER);
        frame.add(logPanel, BorderLayout.SOUTH);

        this.messageArea = (JTextArea) logPanel.getViewport().getView();
        setupPluginListListener();
        refreshFileTree();
    }

    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupPluginListListener() {
        pluginJList.setCellRenderer(new PluginCellRenderer());
        pluginJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Plugin selectedPlugin = pluginJList.getSelectedValue();
                if (selectedPlugin != null) {
                    pluginCardLayout.show(pluginDetailPanel, selectedPlugin.getName());
                }
            }
        });
    }

    private JSplitPane createLeftPanel() {
        JPanel fileExplorerPanel = new JPanel(new BorderLayout());
        fileExplorerPanel.setBorder(new TitledBorder("Explorador de Sesión"));
        fileExplorerPanel.add(new JScrollPane(this.fileTree));

        JPanel pluginListPanel = new JPanel(new BorderLayout());
        pluginListPanel.setBorder(new TitledBorder("Paso 2: Plugins Cargados"));
        pluginListPanel.add(new JScrollPane(this.pluginJList));

        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileExplorerPanel, pluginListPanel);
        leftSplitPane.setDividerLocation(250);
        return leftSplitPane;
    }

    private void onCargarPlugin() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos JAR de Plugin", "jar"));
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File jarFile = chooser.getSelectedFile();
            pluginManager.loadPlugin(jarFile);
            refreshPluginList();
            log(">> Plugins cargados desde: " + jarFile.getName());
        }
    }

    private void refreshPluginList() {
        pluginListModel.clear();
        pluginDetailPanel.removeAll();

        List<Plugin> loadedPlugins = pluginManager.getLoadedPlugins();
        loadedPlugins.forEach(pluginListModel::addElement);

        // ¡Lógica de creación de UI simplificada!
        for (Plugin plugin : loadedPlugins) {
            // Usamos la fábrica para crear el panel. La vista ya no sabe cómo se hace.
            JPanel pluginPanel = pluginPanelFactory.createPanelFor(plugin);
            pluginDetailPanel.add(pluginPanel, plugin.getName());
        }

        pluginDetailPanel.revalidate();
        pluginDetailPanel.repaint();

        if (!loadedPlugins.isEmpty()) {
            pluginJList.setSelectedIndex(0);
            Plugin first = loadedPlugins.get(0);
            pluginCardLayout.show(pluginDetailPanel, first.getName());
        }
    }

    private JPanel createActionPanel(Action recordAction, Action loadAction) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Paso 1: Acciones Principales"));

        panel.add(new JButton(recordAction));
        panel.add(new JButton(loadAction));

        JButton loadPluginButton = new JButton("Cargar Plugin (.jar)");
        loadPluginButton.addActionListener(e -> onCargarPlugin());
        panel.add(loadPluginButton);

        return panel;
    }

    private JScrollPane createLogPanel() {
        JTextArea textArea = new JTextArea(10, 0);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new TitledBorder("Log de Acciones"));
        return scrollPane;
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    public void refreshFileTree() {
        rootNode.removeAllChildren();
        File audioDir = new File(AUDIO_DIRECTORY);
        File[] files = audioDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

        if (files != null) {
            for (File file : files) {
                rootNode.add(new DefaultMutableTreeNode(file.getName()));
            }
        }
        fileTreeModel.reload(rootNode);

        if (rootNode.getChildCount() > 0) {
            fileTree.expandPath(new javax.swing.tree.TreePath(rootNode.getPath()));
        }
        log(">> Explorador de archivos actualizado.");
    }
}