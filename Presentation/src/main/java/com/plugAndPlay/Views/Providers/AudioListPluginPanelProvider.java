package com.plugAndPlay.Views.Providers;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Entities.Audio;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.ListAudiosFromDatabasePlugin;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AudioListPluginPanelProvider implements PluginPanelProvider {

    private final AudioRepository audioRepository;
    private final Runnable fileTreeRefresher;

    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";
    private static final String SESSION_FILE_PATH = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public AudioListPluginPanelProvider(AudioRepository audioRepository, Runnable fileTreeRefresher) {
        this.audioRepository = audioRepository;
        this.fileTreeRefresher = fileTreeRefresher;
    }

    @Override
    public JPanel createPanel(Plugin plugin) {
        ListAudiosFromDatabasePlugin dbPlugin = (ListAudiosFromDatabasePlugin) plugin;

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        DefaultListModel<com.plugAndPlay.Entities.Audio> listModel = new DefaultListModel<>();
        JList<com.plugAndPlay.Entities.Audio> audioJList = new JList<>(listModel);
        audioJList.setCellRenderer(new AudioCellRenderer());
        panel.add(new JScrollPane(audioJList), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refrescar Lista");
        refreshButton.addActionListener(e -> {
            listModel.clear();
            dbPlugin.getAudioList(this.audioRepository).forEach(listModel::addElement);
        });

        JButton loadButton = getJButton(audioJList, dbPlugin, panel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(loadButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        refreshButton.doClick();

        return panel;
    }

    private JButton getJButton(JList<Audio> audioJList, ListAudiosFromDatabasePlugin dbPlugin, JPanel panel) {
        JButton loadButton = new JButton("Cargar seleccionado a la Sesión");
        loadButton.addActionListener(e -> {
            Audio selected = audioJList.getSelectedValue();
            if (selected != null) {
                boolean success = dbPlugin.loadAudioToSessionFile(this.audioRepository, selected.getId(), SESSION_FILE_PATH);
                if (success) {
                    this.fileTreeRefresher.run();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Por favor, seleccione un audio de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        return loadButton;
    }
}

class AudioCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Audio audio) {
            value = audio.getName() + " (ID: " + audio.getId() + ")";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
