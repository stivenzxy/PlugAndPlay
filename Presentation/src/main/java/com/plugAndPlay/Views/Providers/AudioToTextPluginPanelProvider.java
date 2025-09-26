package com.plugAndPlay.Views.Providers;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class AudioToTextPluginPanelProvider implements PluginPanelProvider {
    private final Consumer<String> logger;
    private final AudioRepository audioRepository;

    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";
    private static final String SESSION_FILE_PATH = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public AudioToTextPluginPanelProvider(Consumer<String> logger, AudioRepository audioRepository) {
        this.logger = logger;
        this.audioRepository = audioRepository;
    }

    @Override
    public JPanel createPanel(Plugin plugin) {
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JButton executeButton = new JButton("Ejecutar '" + plugin.getName() + "'");
        panel.add(executeButton, BorderLayout.NORTH);
        
        JTextArea resultArea = new JTextArea(8, 40);
        resultArea.setEditable(false);
        resultArea.setWrapStyleWord(true);
        resultArea.setLineWrap(true);
        resultArea.setBackground(new Color(248, 248, 248));
        resultArea.setBorder(BorderFactory.createTitledBorder("Texto extraído del audio:"));
        resultArea.setText("El texto extraído aparecerá aquí...");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        executeButton.addActionListener(e -> {
            resultArea.setText("Procesando audio...");
            
            AppContext context = new AppContext();
            context.setInputPath(SESSION_FILE_PATH);
            context.registerService(AudioRepository.class, audioRepository);
            
            context.setUiLogger(message -> {
                if (message.contains("Iniciando extracción") ||
                    message.contains("Pasando de audio a texto") ||
                    message.contains("Texto guardado en") ||
                    message.contains("Error:") ||
                    message.contains("No se pudo extraer")) {
                    logger.accept(message);
                }
                
                if (message.startsWith(">> ") && !message.contains("Texto extraído exitosamente:") && 
                    !message.contains("Error:") && !message.contains("Iniciando") && 
                    !message.contains("Pasando") && !message.contains("guardado") &&
                    !message.contains("Carpeta") && !message.contains("Audio de")) {
                    
                    String text = message.substring(3);
                    if (!text.trim().isEmpty() && !text.contains("extraído exitosamente")) {
                        SwingUtilities.invokeLater(() -> {
                            resultArea.setText(text.trim());
                            resultArea.setCaretPosition(0);
                        });
                    }
                }
                
                if (message.contains("Texto guardado en")) {
                    SwingUtilities.invokeLater(() -> {
                        if (resultArea.getText().equals("Procesando audio...")) {
                            resultArea.setText("Texto extraído pero está vacío o no se detectaron palabras.");
                        }
                    });
                } else if (message.contains("No se pudo extraer")) {
                    SwingUtilities.invokeLater(() -> {
                        resultArea.setText("No se pudo extraer texto del audio o el audio no contiene palabras reconocibles.");
                    });
                } else if (message.contains("Error:")) {
                    SwingUtilities.invokeLater(() -> {
                        resultArea.setText("Error: " + message.substring(message.indexOf("Error:") + 6).trim());
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
