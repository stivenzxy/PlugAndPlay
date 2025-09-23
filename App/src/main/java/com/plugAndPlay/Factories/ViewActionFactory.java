package com.plugAndPlay.Factories;

import com.plugAndPlay.Interfaces.AudioLoader;
import com.plugAndPlay.Interfaces.AudioRecorder;
import com.plugAndPlay.Actions.LoadAudioAction;
import com.plugAndPlay.Actions.RecordAudioAction;

import javax.swing.*;
import java.util.function.Consumer;

public class ViewActionFactory {
    private final AudioSourceFactory audioSourceFactory;
    private final Consumer<String> uiLogger;

    public ViewActionFactory(AudioSourceFactory audioSourceFactory, Consumer<String> uiLogger) {
        this.audioSourceFactory = audioSourceFactory;
        this.uiLogger = uiLogger;
    }

    public AbstractAction createRecordAudioAction() {
        AudioRecorder recorder = audioSourceFactory.createAudioRecorder();
        return new RecordAudioAction(recorder, uiLogger);
    }

    public AbstractAction createLoadAudioAction(JFrame parentFrame) {
        AudioLoader loader = audioSourceFactory.createAudioLoader();
        return new LoadAudioAction(parentFrame, loader, uiLogger);
    }
}
