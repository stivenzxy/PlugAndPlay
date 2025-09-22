package com.plugAndPlay.Views;

import com.plugAndPlay.Factories.AudioSourceFactory;
import com.plugAndPlay.Interfaces.AudioLoader;
import com.plugAndPlay.Interfaces.AudioRecorder;
import com.plugAndPlay.Views.Actions.LoadAudioAction;
import com.plugAndPlay.Views.Actions.RecordAudioAction;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MainView {

    private final JFrame frame;
    private final JTextArea messageArea;

    private final Action recordAudioAction;
    private final Action loadAudioAction;

    public MainView(AudioSourceFactory factory) {
        frame = new JFrame("Plug & Play - Core");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout(10, 10));

        AudioRecorder recordAudioUseCase = factory.createAudioRecorder();
        AudioLoader loadAudioUseCase = factory.createAudioLoader();

        this.recordAudioAction = new RecordAudioAction(recordAudioUseCase, this::log);
        this.loadAudioAction = new LoadAudioAction(this.frame, loadAudioUseCase, this::log);

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