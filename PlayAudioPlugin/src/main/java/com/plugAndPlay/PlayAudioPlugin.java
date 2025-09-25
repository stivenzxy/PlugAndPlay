package com.plugAndPlay;

import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;

public class PlayAudioPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(PlayAudioPlugin.class);

    @Override
    public String getName() {
        return "Reproducir Audio";
    }

    @Override
    public void execute(AppContext context) {
        String audioFilePath = context.getInputPath();

        if (audioFilePath == null || audioFilePath.isEmpty()) {
            logger.error("No se especificó una ruta de archivo de audio en el contexto.");
            context.getUiLogger().accept(">> Error: No se ha cargado ningún audio para reproducir.");
            return;
        }

        logger.info("Ejecutando el plugin de reproducción para: {}", audioFilePath);
        context.getUiLogger().accept(">> Iniciando reproducción de " + new File(audioFilePath).getName() + "...");

        File audioFile = new File(audioFilePath);

        if (!audioFile.exists()) {
            logger.error("Archivo de audio no encontrado: {}", audioFilePath);
            context.getUiLogger().accept(">> Error: El archivo de sesión no se encontró en el disco.");
            return;
        }

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile)) {
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);

            audioLine.open(format);
            audioLine.start();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
                audioLine.write(buffer, 0, bytesRead);
            }

            audioLine.drain();
            audioLine.close();
            logger.info("Reproducción finalizada exitosamente.");
            context.getUiLogger().accept(">> Reproducción finalizada.");
        } catch (Exception e) {
            logger.error("Error crítico durante la reproducción del audio.", e);
            context.getUiLogger().accept(">> Error al reproducir el audio: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java com.plugAndPlay.Plugins.PlayAudioPlugin <ruta_al_archivo.wav>");
            return;
        }

        AppContext consoleContext = new AppContext();
        consoleContext.setInputPath(args[0]);

        PlayAudioPlugin plugin = new PlayAudioPlugin();

        plugin.execute(consoleContext);
    }
}
