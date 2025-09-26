package com.plugAndPlay.Factories;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.ListAudiosFromDatabasePlugin;
import com.plugAndPlay.Views.Providers.AudioListPluginPanelProvider;
import com.plugAndPlay.Views.Providers.GenericPluginPanelProvider;
import com.plugAndPlay.Views.Providers.PluginPanelProvider;

import javax.swing.*;
import java.util.function.Consumer;

public class PluginPanelFactory {
    private final PluginPanelProvider genericProvider;
    private final PluginPanelProvider AudioListProvider;

    public PluginPanelFactory(Consumer<String> logger, AudioRepository audioRepository, Runnable fileTreeRefresher) {
        this.genericProvider = new GenericPluginPanelProvider(logger, audioRepository);
        this.AudioListProvider = new AudioListPluginPanelProvider(audioRepository, fileTreeRefresher);
    }

    public JPanel createPanelFor(Plugin plugin) {
        if (plugin instanceof ListAudiosFromDatabasePlugin) {
            return AudioListProvider.createPanel(plugin);
        } else {
            return genericProvider.createPanel(plugin);
        }
    }
}