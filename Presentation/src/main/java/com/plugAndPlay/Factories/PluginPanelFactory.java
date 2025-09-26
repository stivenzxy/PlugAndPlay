package com.plugAndPlay.Factories;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.ListAudiosFromDatabasePlugin;
import com.plugAndPlay.Views.Providers.AudioListPluginPanelProvider;
import com.plugAndPlay.Views.Providers.AudioToTextPluginPanelProvider;
import com.plugAndPlay.Views.Providers.GenericPluginPanelProvider;
import com.plugAndPlay.Views.Providers.PluginPanelProvider;
import com.plugAndPlay.Views.Providers.SearchTextPluginPanelProvider;

import javax.swing.*;
import java.util.function.Consumer;

public class PluginPanelFactory {
    private final PluginPanelProvider genericProvider;
    private final PluginPanelProvider AudioListProvider;
    private final PluginPanelProvider audioToTextProvider;
    private final PluginPanelProvider searchTextProvider;

    public PluginPanelFactory(Consumer<String> logger, AudioRepository audioRepository, Runnable fileTreeRefresher) {
        this.genericProvider = new GenericPluginPanelProvider(logger, audioRepository);
        this.AudioListProvider = new AudioListPluginPanelProvider(audioRepository, fileTreeRefresher);
        this.audioToTextProvider = new AudioToTextPluginPanelProvider(logger, audioRepository);
        this.searchTextProvider = new SearchTextPluginPanelProvider(logger, audioRepository);
    }

    public JPanel createPanelFor(Plugin plugin) {
        try {
            if (plugin instanceof ListAudiosFromDatabasePlugin) {
                return AudioListProvider.createPanel(plugin);
            } else if ("Extraer Texto del Audio".equals(plugin.getName())) {
                return audioToTextProvider.createPanel(plugin);
            } else if ("Buscar Texto".equals(plugin.getName())) {
                return searchTextProvider.createPanel(plugin);
            } else {
                return genericProvider.createPanel(plugin);
            }
        } catch (Exception e) {
            return genericProvider.createPanel(plugin);
        }
    }
    
    public PluginPanelProvider getGenericProvider() {
        return genericProvider;
    }
}