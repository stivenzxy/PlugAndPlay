package com.plugAndPlay.Plugin;

import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;

import javax.sound.sampled.*;
import java.io.File;

public class PlayAudioPlugin implements Plugin {
    @Override
    public String getName() {
        return "Reproducir Audio";
    }

    @Override
    public void execute(AppContext context) {
        String audioFilePath = context.getInputPath();

        if (audioFilePath == null || audioFilePath.isEmpty()) {
            System.err.println("Error: No se especificó una ruta de archivo de audio en el contexto.");
            return;
        }

        System.out.println("Ejecutando el plugin de reproducción para: " + audioFilePath);
        File audioFile = new File(audioFilePath);

        if (!audioFile.exists()) {
            System.err.println("Archivo de sesión no encontrado: " + audioFilePath);
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
            System.out.println("Reproducción finalizada.");
            context.getUiLogger().accept(">> Reproducción finalizada para " + audioFile.getName());

        } catch (Exception e) {
            System.err.println("Error al reproducir el audio: " + e.getMessage());
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
