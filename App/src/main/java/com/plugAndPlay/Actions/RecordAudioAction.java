package com.plugAndPlay.Actions;

import com.plugAndPlay.Interfaces.AudioRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.function.Consumer;

public class RecordAudioAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(RecordAudioAction.class);

    private final AudioRecorder recordAudio;
    private final Consumer<String> uiLogger;

    private Timer timer;
    private int elapsedSeconds;

    public RecordAudioAction(AudioRecorder recordAudioUseCase, Consumer<String> uiLogger) {
        super("Grabar Audio");
        this.uiLogger = uiLogger;
        this.recordAudio = recordAudioUseCase;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!recordAudio.isRecording()) {
            startRecording();
        } else {
            stopAndSaveRecording();
        }
    }

    private void startRecording() {
        recordAudio.start();
        elapsedSeconds = 0;

        timer = new Timer(1000, evt -> {
            elapsedSeconds++;
            putValue(Action.NAME, "Detener Grabación (" + formatTime(elapsedSeconds) + ")");
        });
        timer.start();

        putValue(Action.NAME, "Detener Grabación (00:00)");
        uiLogger.accept(">> Grabación iniciada...");
        logger.info("Grabación iniciada por el usuario en GUI.");
    }

    private void stopAndSaveRecording() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        File recordedAudio = recordAudio.stop();
        if (recordedAudio != null) {
            uiLogger.accept(">> Grabación guardada: " + recordedAudio);
        } else {
            logger.error("Se detuvo la grabación pero el stream de audio era nulo.");
            uiLogger.accept(">> Error: No se pudo obtener el audio grabado.");
        }
        putValue(Action.NAME, "Grabar Audio");
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}