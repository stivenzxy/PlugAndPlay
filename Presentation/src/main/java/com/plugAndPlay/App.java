package com.plugAndPlay;

import com.plugAndPlay.Facade.PluginManager;
import com.plugAndPlay.Factories.AudioSourceFactory;
import com.plugAndPlay.Environment.EnvironmentInitializer;
import com.plugAndPlay.Views.MainView;

import javax.swing.*;

public class CoreApp {
    public static void main(String[] args) {
        EnvironmentInitializer.initEnvironment();
        AudioSourceFactory audioSourceFactory = new AudioSourceFactory();
        PluginManager pluginManager = new PluginManager();

        SwingUtilities.invokeLater(() -> {
            MainView view = new MainView(audioSourceFactory, pluginManager);
            view.show();
        });
    }
}