package com.plugAndPlay;

import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextToAudioPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(TextToAudioPlugin.class);
    private static final String SESSION_TEXT_PATH = "Texto/session_text.txt";
    
    @Override
    public String getName() {
        return "Texto a Audio";
    }

    @Override
    public void execute(AppContext context) {
        var uiLogger = context.getUiLogger();
        
        uiLogger.accept(">> Iniciando conversión de texto a audio...");

        // Determinar la ruta del archivo de texto
        String textFilePath = context.getInputPath();
        if (textFilePath == null || textFilePath.isEmpty()) {
            textFilePath = SESSION_TEXT_PATH;
        }

        File textFile = new File(textFilePath);
        if (!textFile.exists()) {
            uiLogger.accept(">> Error: No se encontró el archivo de texto: " + textFilePath);
            logger.error("Archivo de texto no encontrado: {}", textFilePath);
            return;
        }

        try {
            String text = Files.readString(Paths.get(textFilePath)).trim();
            if (text.isEmpty()) {
                uiLogger.accept(">> Error: El archivo de texto está vacío");
                logger.warn("Archivo de texto vacío: {}", textFilePath);
                return;
            }

            uiLogger.accept(">> Texto encontrado: " + text);
            
            String outputPath = context.getOutputPath();
            if (outputPath == null || outputPath.isEmpty()) {
                outputPath = promptUserForOutputPath();
                
                if (outputPath == null) {
                    uiLogger.accept(">> Generación de audio cancelada por el usuario");
                    return;
                }
            }
            
            uiLogger.accept(">> Configurando sintetizador de voz...");
            uiLogger.accept(">> Guardando en: " + outputPath);

            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            
            boolean success = generateAudioWithSystemTTS(text, outputFile.getAbsolutePath(), uiLogger);
            
            if (!success) {
                success = generateAudioWithDirectCommand(text, outputFile.getAbsolutePath(), uiLogger);
            }
            
            if (outputFile.exists() && outputFile.length() > 0) {
                context.setInputPath(outputPath);
                
                uiLogger.accept(">> Audio generado exitosamente en: " + outputPath);
                logger.info("Audio generado correctamente en: {}", outputPath);
            } else {
                uiLogger.accept(">> Error: No se pudo generar el archivo de audio");
                logger.error("El archivo de audio no se generó correctamente");
            }
            
        } catch (IOException e) {
            uiLogger.accept(">> Error al leer el archivo de texto: " + e.getMessage());
            logger.error("Error leyendo archivo de texto", e);
        } catch (Exception e) {
            uiLogger.accept(">> Error al generar audio: " + e.getMessage());
            logger.error("Error crítico durante la generación de audio", e);
        }
    }

    private String promptUserForOutputPath() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Audio Generado");
            fileChooser.setSelectedFile(new File("audio_generado.wav"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos WAV", "wav"));
            
            int result = fileChooser.showSaveDialog(null);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                
                if (!path.toLowerCase().endsWith(".wav")) {
                    path += ".wav";
                }
                
                return path;
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error al mostrar diálogo de guardado", e);
            return "Audio/audio_generado.wav";
        }
    }

    private boolean generateAudioWithSystemTTS(String text, String outputPath, java.util.function.Consumer<String> uiLogger) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                uiLogger.accept(">> Usando sintetizador de Windows...");
                String[] command = {
                    "powershell", "-Command",
                    "Add-Type -AssemblyName System.Speech; " +
                    "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$synth.Rate = -1; " +
                    "$synth.SetOutputToWaveFile('" + outputPath + "'); " +
                    "$synth.Speak('" + text.replace("'", "''") + "'); " +
                    "$synth.Dispose()"
                };
                
                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();
                return exitCode == 0;
            } else {
                uiLogger.accept(">> Usando espeak...");
                String[] command = {"espeak", "-s", "120", "-v", "es", "-w", outputPath, text};
                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();
                return exitCode == 0;
            }
        } catch (Exception e) {
            logger.error("Error usando TTS del sistema", e);
            return false;
        }
    }

    private boolean generateAudioWithDirectCommand(String text, String outputPath, java.util.function.Consumer<String> uiLogger) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                uiLogger.accept(">> Generando audio básico...");
                
                File tempTxtFile = File.createTempFile("tts_text", ".txt");
                Files.write(tempTxtFile.toPath(), text.getBytes("UTF-8"));
                
                String[] command = {
                    "cmd", "/c", 
                    "echo " + text + " > temp.txt && " +
                    "powershell -Command \"" +
                    "Add-Type -AssemblyName System.Speech; " +
                    "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$synth.Rate = -1; " +
                    "$synth.SetOutputToWaveFile('" + outputPath + "'); " +
                    "$synth.Speak('" + text.replace("\"", "'") + "'); " +
                    "$synth.Dispose()\""
                };
                
                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();
                
                tempTxtFile.delete();
                return exitCode == 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error en comando directo", e);
            return false;
        }
    }

    public static void main(String[] args) {
        AppContext consoleContext = new AppContext();
        consoleContext.setUiLogger(System.out::println);

        if (args.length > 0) {
            consoleContext.setInputPath(args[0]);
            System.out.println(">> Usando archivo de texto: " + args[0]);
        } else {
            System.out.println(">> Usando archivo de texto por defecto: " + SESSION_TEXT_PATH);
            System.out.println(">> Uso: java -jar TextToAudioPlugin.jar [ruta_al_archivo.txt]");
        }

        TextToAudioPlugin plugin = new TextToAudioPlugin();
        plugin.execute(consoleContext);
    }
}
