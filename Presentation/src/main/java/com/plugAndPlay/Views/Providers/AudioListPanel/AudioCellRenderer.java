package com.plugAndPlay.Views.Providers.AudioListPanel;

import com.plugAndPlay.Entities.Audio;

import javax.swing.*;
import java.awt.*;

class AudioCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Audio audio) {
            value = audio.getName() + " (ID: " + audio.getId() + ")";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}