package com.plugAndPlay.Interfaces;

import java.io.File;

public interface AudioRecorder {
    void start();
    File stop();
    boolean isRecording();
}
