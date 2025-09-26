package com.plugAndPlay.Views.Providers.AudioListPanel;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Entities.Audio;
import com.plugAndPlay.Interfaces.AudioListListener;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Interfaces.SelectedAudioProvider;
import com.plugAndPlay.Shared.AppContext;
import com.plugAndPlay.Views.Providers.PluginPanelProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class AudioListPluginPanelProvider implements PluginPanelProvider {

    private final Consumer<String> logger;
    private final AudioRepository audioRepository;
    private static final String SESSION_PATH = "Audio" + File.separator + "session_audio.wav";

    public AudioListPluginPanelProvider(Consumer<String> logger, AudioRepository audioRepository) {
        this.logger = logger;
        this.audioRepository = audioRepository;
    }

    @Override
    public JPanel createPanel(Plugin plugin) {
        JPanel panel = new JPanel(new BorderLayout(6,6));

        DefaultListModel<Audio> listModel = new DefaultListModel<>();
        JList<Audio> audioJList = new JList<>(listModel);
        audioJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        audioJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel("ID: " + value.getId() + " | " + value.getName() + " (" + value.getFormat() + ")");
            if (isSelected) label.setOpaque(true);
            return label;
        });

        JScrollPane listScroll = new JScrollPane(audioJList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Audios en BD"));

        JTextArea logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createTitledBorder("Mensajes"));
        JScrollPane logScroll = new JScrollPane(logArea);

        JButton btnRefresh = new JButton("Refrescar lista");
        JButton btnLoad = new JButton("Cargar seleccionado a sesión");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnRefresh);
        top.add(btnLoad);

        panel.add(top, BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);
        panel.add(logScroll, BorderLayout.SOUTH);

        // uiLogger que envía al area y al logger global
        Consumer<String> uiLogger = msg -> SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            logger.accept(msg); // también pasa al logger principal (si quieres)
        });

        // Listener que actualiza la lista (se registra en el contexto antes de ejecutar el plugin)
        AudioListListener listListener = audios -> SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (Audio a : audios) listModel.addElement(a);
        });

        // ---- Botón refrescar: registra sólo el listener y ejecuta el plugin ----
        btnRefresh.addActionListener(e -> {
            logArea.setText("");
            AppContext ctx = new AppContext();
            ctx.registerService(AudioRepository.class, audioRepository);
            ctx.setUiLogger(uiLogger);
            ctx.registerService(AudioListListener.class, listListener);

            // Ejecuta el plugin en background
            new Thread(() -> {
                try {
                    plugin.execute(ctx);
                } catch (Exception ex) {
                    uiLogger.accept("Error inesperado: " + ex.getMessage());
                }
            }).start();
        });

        // ---- Botón cargar seleccionado: registra listener + SelectedAudioProvider ----
        btnLoad.addActionListener(e -> {
            Audio selected = audioJList.getSelectedValue();
            if (selected == null) {
                uiLogger.accept("Seleccione un audio antes de cargarlo.");
                return;
            }

            AppContext ctx = new AppContext();
            ctx.registerService(AudioRepository.class, audioRepository);
            ctx.setUiLogger(uiLogger);
            ctx.registerService(AudioListListener.class, listListener);

            // SelectedAudioProvider devuelve el id seleccionado
            SelectedAudioProvider selectedProvider = selected::getId;
            ctx.registerService(SelectedAudioProvider.class, selectedProvider);

            new Thread(() -> {
                try {
                    plugin.execute(ctx);
                } catch (Exception ex) {
                    uiLogger.accept("Error inesperado: " + ex.getMessage());
                }
            }).start();
        });

        return panel;
    }
}


