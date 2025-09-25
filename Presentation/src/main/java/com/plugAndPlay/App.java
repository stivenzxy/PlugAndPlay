package com.plugAndPlay;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Data.Repository.AudioRepositoryImpl;
import com.plugAndPlay.Facade.PluginManager;
import com.plugAndPlay.Factories.AudioSourceFactory;
import com.plugAndPlay.Environment.EnvironmentInitializer;
import com.plugAndPlay.Views.MainView;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        EnvironmentInitializer.initEnvironment();
        AudioSourceFactory audioSourceFactory = new AudioSourceFactory();
        AudioRepository audioRepository = new AudioRepositoryImpl();

        PluginManager pluginManager = new PluginManager();

        SwingUtilities.invokeLater(() -> {
            MainView view = new MainView(audioSourceFactory, pluginManager, audioRepository);
            view.show();
        });
    }
}