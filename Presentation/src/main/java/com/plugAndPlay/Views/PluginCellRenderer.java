package com.plugAndPlay.Views;

import com.plugAndPlay.Interfaces.Plugin;

import javax.swing.*;
import java.awt.*;

class PluginCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Plugin) {
            value = ((Plugin) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}