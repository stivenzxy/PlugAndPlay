package com.plugAndPlay.UseCases;

import com.plugAndPlay.Interfaces.AudioRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class RecordAudio implements AudioRecorder {

    private static final Logger logger = LoggerFactory.getLogger(RecordAudio.class);
    private final File sessionFile;

    private boolean isRecording = false;
    private TargetDataLine audioLine;
    private ByteArrayOutputStream byteArrayOutputStream;

    public RecordAudio(String sessionFilePath) {
        this.sessionFile = new File(sessionFilePath);
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(44100, 16, 2, true, false);
    }

    public void start() {
        if (isRecording) {
            logger.warn("Se intentó iniciar una grabación mientras ya se estaba grabando.");
        }

        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException("Línea de grabación no soportada.");
            }

            audioLine = (TargetDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();

            byteArrayOutputStream = new ByteArrayOutputStream();
            isRecording = true;

            Thread recordingThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (isRecording) {
                    int bytesRead = audioLine.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            });
            recordingThread.start();
            logger.info("Grabación iniciada correctamente.");
        } catch (LineUnavailableException e) {
            logger.error("No se pudo acceder a la línea de audio para grabar.", e);
        }
    }

    public File stop() {
        if (!isRecording) return null;

        isRecording = false;
        audioLine.stop();
        audioLine.close();
        logger.info("Grabación detenida.");

        try (AudioInputStream audioStream = new AudioInputStream(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                getAudioFormat(),
                byteArrayOutputStream.size() / getAudioFormat().getFrameSize()
        )) {
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, this.sessionFile);
            logger.info("Audio de sesión guardado en: {}", this.sessionFile.getAbsolutePath());
            return this.sessionFile;
        } catch (IOException e) {
            logger.error("Error de E/S al intentar guardar el archivo de sesión en {}", this.sessionFile.getAbsolutePath(), e);
            return null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}