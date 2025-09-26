package com.plugAndPlay.Views.Providers;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class SearchTextPluginPanelProvider implements PluginPanelProvider {
    private final Consumer<String> logger;
    private final AudioRepository audioRepository;

    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";
    private static final String SESSION_FILE_PATH = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public SearchTextPluginPanelProvider(Consumer<String> logger, AudioRepository audioRepository) {
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
        resultArea.setBorder(BorderFactory.createTitledBorder("Resultados de búsqueda:"));
        resultArea.setText("Los resultados de búsqueda aparecerán aquí...");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        executeButton.addActionListener(e -> {
            resultArea.setText("Procesando búsqueda...");
            
            AppContext context = new AppContext();
            context.setInputPath(SESSION_FILE_PATH);
            context.registerService(AudioRepository.class, audioRepository);
            
            context.setUiLogger(message -> {
                if (message.contains("Iniciando búsqueda") ||
                    message.contains("Error:") ||
                    message.contains("Búsqueda cancelada")) {
                    logger.accept(message);
                }
                
                if (message.contains("========== RESULTADOS DE BÚSQUEDA ==========") ||
                    message.contains("COINCIDENCIAS EXACTAS") ||
                    message.contains("COINCIDENCIAS SIMILARES") ||
                    message.contains("No se encontraron coincidencias") ||
                    message.contains("Sugerencias:") ||
                    message.contains("==========================================") ||
                    (message.startsWith(">>   ") && (message.contains("Posición") || message.contains("Palabra similar")))) {
                    
                    SwingUtilities.invokeLater(() -> {
                        String currentText = resultArea.getText();
                        if (currentText.equals("Procesando búsqueda...")) {
                            resultArea.setText("");
                        }
                        
                        String cleanMessage = message.startsWith(">> ") ? message.substring(3) : message;
                        resultArea.append(cleanMessage + "\n");
                        resultArea.setCaretPosition(resultArea.getDocument().getLength());
                    });
                }
                
                if (message.contains("Error:") && !message.contains("No se encontró archivo de texto")) {
                    SwingUtilities.invokeLater(() -> {
                        String cleanMessage = message.startsWith(">> ") ? message.substring(3) : message;
                        resultArea.setText("Error: " + cleanMessage);
                    });
                }
                
                if (message.contains("No se encontró archivo de texto")) {
                    SwingUtilities.invokeLater(() -> {
                        resultArea.setText("Error: No se encontró archivo de texto.\n" +
                                        "Ejecute primero el plugin 'Extraer Texto del Audio' para generar el archivo de texto.");
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
