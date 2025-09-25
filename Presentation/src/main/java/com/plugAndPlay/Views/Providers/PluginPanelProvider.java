package com.plugAndPlay.Views.Providers;

import com.plugAndPlay.Interfaces.Plugin;

import javax.swing.*;

@FunctionalInterface
public interface PluginPanelProvider {
    JPanel createPanel(Plugin plugin);
}
