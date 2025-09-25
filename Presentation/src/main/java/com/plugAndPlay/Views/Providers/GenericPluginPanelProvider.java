package com.plugAndPlay.Views.Providers;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class GenericPluginPanelProvider implements PluginPanelProvider {
    private final Consumer<String> logger;
    private final AudioRepository audioRepository;

    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";
    private static final String SESSION_FILE_PATH = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public GenericPluginPanelProvider(Consumer<String> logger, AudioRepository audioRepository) {
        this.logger = logger;
        this.audioRepository = audioRepository;
    }

    @Override
    public JPanel createPanel(Plugin plugin) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton executeButton = new JButton("Ejecutar '" + plugin.getName() + "'");

        executeButton.addActionListener(e -> {
            AppContext context = new AppContext();
            context.setInputPath(SESSION_FILE_PATH);
            context.setUiLogger(logger);
            context.registerService(AudioRepository.class, audioRepository);
            plugin.execute(context);
        });

        panel.add(executeButton);
        return panel;
    }
}
