package com.plugAndPlay.Views;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Facade.PluginManager;
import com.plugAndPlay.Factories.AudioSourceFactory;
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


    private final JTree fileTree;
    private final DefaultTreeModel fileTreeModel;
    private final DefaultMutableTreeNode rootNode;
    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";
    private static final String SESSION_FILE_PATH = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public MainView(AudioSourceFactory audioSourceFactory, PluginManager pluginManager, AudioRepository audioRepository) {
        this.pluginManager = pluginManager;
        this.audioRepository = audioRepository;
        
        pluginManager.setUiLogger(this::log);

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

        for (Plugin plugin : loadedPlugins) {
            try {
                if (plugin instanceof com.plugAndPlay.ListAudiosFromDatabasePlugin) {
                    JPanel dbPanel = createDBPluginPanel((com.plugAndPlay.ListAudiosFromDatabasePlugin) plugin);
                    pluginDetailPanel.add(dbPanel, plugin.getName());
                } else {
                    JPanel genericPanel = createGenericPluginPanel(plugin);
                    pluginDetailPanel.add(genericPanel, plugin.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private JPanel createGenericPluginPanel(Plugin plugin) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton executeButton = new JButton("Ejecutar '" + plugin.getName() + "'");

        executeButton.addActionListener(e -> {
            AppContext context = new AppContext();
            context.setInputPath(SESSION_FILE_PATH);
            context.setUiLogger(this::log);
            context.registerService(AudioRepository.class, this.audioRepository);

            plugin.execute(context);
        });

        panel.add(executeButton);
        return panel;
    }

    private JPanel createDBPluginPanel(com.plugAndPlay.ListAudiosFromDatabasePlugin plugin) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        DefaultListModel<com.plugAndPlay.Entities.Audio> listModel = new DefaultListModel<>();
        JList<com.plugAndPlay.Entities.Audio> audioJList = new JList<>(listModel);
        audioJList.setCellRenderer(new AudioCellRenderer());
        panel.add(new JScrollPane(audioJList), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refrescar Lista");
        refreshButton.addActionListener(e -> {
            listModel.clear();
            // Llama al método público específico del plugin
            plugin.getAudioList(this.audioRepository).forEach(listModel::addElement);
        });

        JButton loadButton = new JButton("Cargar seleccionado a la Sesión");
        loadButton.addActionListener(e -> {
            com.plugAndPlay.Entities.Audio selected = audioJList.getSelectedValue();
            if (selected != null) {
                // Llama al otro método público específico del plugin
                boolean success = plugin.loadAudioToSessionFile(this.audioRepository, selected.getId(), SESSION_FILE_PATH);
                if (success) {
                    this.refreshFileTree();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Por favor, seleccione un audio de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(loadButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Cargar la lista al iniciar
        refreshButton.doClick();

        return panel;
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

class AudioCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof com.plugAndPlay.Entities.Audio) {
            com.plugAndPlay.Entities.Audio audio = (com.plugAndPlay.Entities.Audio) value;
            value = audio.getName() + " (ID: " + audio.getId() + ")";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
