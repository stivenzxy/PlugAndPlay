package com.plugAndPlay.Factories;

import com.plugAndPlay.Interfaces.AudioLoader;
import com.plugAndPlay.Interfaces.AudioRecorder;
import com.plugAndPlay.Views.Actions.LoadAudioAction;
import com.plugAndPlay.Views.Actions.RecordAudioAction;

import javax.swing.*;
import java.util.function.Consumer;

public class ViewActionFactory {
    private final AudioSourceFactory audioSourceFactory;
    private final Consumer<String> uiLogger;

    private final Runnable onActionCompleteCallback;

    public ViewActionFactory(AudioSourceFactory audioSourceFactory, Consumer<String> uiLogger, Runnable onActionCompleteCallback) {
        this.audioSourceFactory = audioSourceFactory;
        this.uiLogger = uiLogger;
        this.onActionCompleteCallback = onActionCompleteCallback;
    }

    public AbstractAction createRecordAudioAction() {
        AudioRecorder recorder = audioSourceFactory.createAudioRecorder();
        return new RecordAudioAction(recorder, uiLogger, onActionCompleteCallback);
    }

    public AbstractAction createLoadAudioAction(JFrame parentFrame) {
        AudioLoader loader = audioSourceFactory.createAudioLoader();
        return new LoadAudioAction(parentFrame, loader, uiLogger, onActionCompleteCallback);
    }
}
