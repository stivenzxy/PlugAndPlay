package com.plugAndPlay.Views.Providers;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class TextToAudioPluginPanelProvider implements PluginPanelProvider {
    private final Consumer<String> logger;
    private final AudioRepository audioRepository;

    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";
    private static final String SESSION_FILE_PATH = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public TextToAudioPluginPanelProvider(Consumer<String> logger, AudioRepository audioRepository) {
        this.logger = logger;
        this.audioRepository = audioRepository;
    }

    @Override
    public JPanel createPanel(Plugin plugin) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton executeButton = new JButton("Ejecutar '" + plugin.getName() + "'");
        topPanel.add(executeButton);
        panel.add(topPanel, BorderLayout.NORTH);
        
        JTextArea resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        resultArea.setWrapStyleWord(true);
        resultArea.setLineWrap(true);
        resultArea.setBackground(new Color(248, 248, 248));
        resultArea.setBorder(BorderFactory.createTitledBorder("Información y resultados:"));
        resultArea.setText("Los resultados de la conversión aparecerán aquí...");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        executeButton.addActionListener(e -> {
            resultArea.setText("Procesando conversión de texto a audio...");
            
            AppContext context = new AppContext();
            context.setInputPath("Texto/session_text.txt");
            context.setOutputPath("Audio/audio_generado.wav");
            context.registerService(AudioRepository.class, audioRepository);
            
            context.setUiLogger(message -> {
                if (message.contains("Iniciando conversión") ||
                    message.contains("Texto encontrado") ||
                    message.contains("Configurando sintetizador") ||
                    message.contains("Guardando en") ||
                    message.contains("Audio generado exitosamente") ||
                    message.contains("Error:") ||
                    message.contains("Generación de audio cancelada")) {
                    logger.accept(message);
                }
                
                if (message.contains("Texto encontrado") ||
                    message.contains("Audio generado exitosamente") ||
                    message.contains("Error:") ||
                    message.contains("Generación de audio cancelada") ||
                    message.contains("No se pudo generar")) {
                    
                    SwingUtilities.invokeLater(() -> {
                        String currentText = resultArea.getText();
                        if (currentText.equals("Procesando conversión de texto a audio...")) {
                            resultArea.setText("");
                        }
                        
                        String cleanMessage = message.startsWith(">> ") ? message.substring(3) : message;
                        resultArea.append(cleanMessage + "\n");
                        resultArea.setCaretPosition(resultArea.getDocument().getLength());
                    });
                }
            });
            
            SwingUtilities.invokeLater(() -> {
                new Thread(() -> {
                    try {
                        plugin.execute(context);
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            resultArea.setText("Error inesperado: " + ex.getMessage());
                            logger.accept(">> Error inesperado: " + ex.getMessage());
                        });
                    }
                }).start();
            });
        });
        
        return panel;
    }
}
