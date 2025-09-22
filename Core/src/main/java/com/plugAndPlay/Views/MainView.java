package com.plugAndPlay.Views;

import com.plugAndPlay.Factories.AudioSourceFactory;
import com.plugAndPlay.Factories.ViewActionFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MainView {

    private final JFrame frame;
    private final JTextArea messageArea;

    private final Action recordAudioAction;
    private final Action loadAudioAction;

    public MainView(AudioSourceFactory audioSourceFactory) {
        frame = new JFrame("Plug & Play - Core");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout(10, 10));

        ViewActionFactory viewActionFactory = new ViewActionFactory(audioSourceFactory, this::log);

        this.recordAudioAction = viewActionFactory.createRecordAudioAction();
        this.loadAudioAction = viewActionFactory.createLoadAudioAction(this.frame);

        JScrollPane logPanel = createLogPanel();
        JPanel buttonPanel = createActionPanel();

        frame.add(logPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        this.messageArea = (JTextArea) logPanel.getViewport().getView();
    }

    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton grabarBtn = new JButton(recordAudioAction);
        JButton cargarBtn = new JButton(loadAudioAction);

        panel.add(grabarBtn);
        panel.add(cargarBtn);
        return panel;
    }

    private JScrollPane createLogPanel() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new TitledBorder("Log de Acciones"));
        return scrollPane;
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
}