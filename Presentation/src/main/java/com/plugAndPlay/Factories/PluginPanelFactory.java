package com.plugAndPlay.Factories;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Views.Providers.AudioListPanel.AudioListPluginPanelProvider;
import com.plugAndPlay.Views.Providers.AudioToTextPluginPanelProvider;
import com.plugAndPlay.Views.Providers.GenericPluginPanelProvider;
import com.plugAndPlay.Views.Providers.PluginPanelProvider;
import com.plugAndPlay.Views.Providers.SearchTextPluginPanelProvider;
import com.plugAndPlay.Views.Providers.TextToAudioPluginPanelProvider;

import javax.swing.*;
import java.util.function.Consumer;

public class PluginPanelFactory {
    private final PluginPanelProvider genericProvider;
    private final PluginPanelProvider audioListProvider;
    private final PluginPanelProvider audioToTextProvider;
    private final PluginPanelProvider searchTextProvider;
    private final PluginPanelProvider textToAudioProvider;

    public PluginPanelFactory(Consumer<String> logger, AudioRepository audioRepository, Runnable fileTreeRefresher) {
        this.genericProvider = new GenericPluginPanelProvider(logger, audioRepository);
        this.audioListProvider = new AudioListPluginPanelProvider(logger, audioRepository);
        this.audioToTextProvider = new AudioToTextPluginPanelProvider(logger, audioRepository);
        this.searchTextProvider = new SearchTextPluginPanelProvider(logger, audioRepository);
        this.textToAudioProvider = new TextToAudioPluginPanelProvider(logger, audioRepository);
    }

    public JPanel createPanelFor(Plugin plugin) {
        String pluginName = plugin.getName();

        return switch (pluginName) {
            case "Extraer Texto del Audio" -> audioToTextProvider.createPanel(plugin);
            case "Buscar Texto" -> searchTextProvider.createPanel(plugin);
            case "Texto a Audio" -> textToAudioProvider.createPanel(plugin);
            case "Cargar Listado de audios desde BD" -> audioListProvider.createPanel(plugin);
            case null, default -> genericProvider.createPanel(plugin);
        };
    }
    
    public PluginPanelProvider getGenericProvider() {
        return genericProvider;
    }
}