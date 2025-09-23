package com.plugAndPlay;

import com.plugAndPlay.Factories.AudioSourceFactory;
import com.plugAndPlay.Utils.EnvironmentInitializer;

import javax.swing.*;

public class CoreApp {
    public static void main(String[] args) {
        EnvironmentInitializer.initEnvironment();
        AudioSourceFactory audioSourceFactory = new AudioSourceFactory();

        SwingUtilities.invokeLater(() -> {
            MainView view = new MainView(audioSourceFactory);
            view.show();
        });
    }
}