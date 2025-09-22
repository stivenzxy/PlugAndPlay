package com.plugAndPlay.UseCases;

import com.plugAndPlay.Interfaces.AudioLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class LoadAudio implements AudioLoader {

    private static final Logger logger = LoggerFactory.getLogger(LoadAudio.class);
    private final File sessionFile;

    public LoadAudio(String sessionFilePath) {
        this.sessionFile = new File(sessionFilePath);
    }

    @Override
    public File execute(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new RuntimeException("El archivo de audio de origen no existe o es nulo.");
        }

        logger.info("Cargando archivo '{}' en el workspace...", sourceFile.getName());

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(sourceFile)) {
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, this.sessionFile);
            logger.info("Archivo copiado exitosamente al archivo de sesión: {}", this.sessionFile.getAbsolutePath());
            return this.sessionFile;
        } catch (IOException | UnsupportedAudioFileException e) {
            logger.error("Error al procesar el archivo de audio: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo procesar el archivo de audio.", e);
        }
    }
}