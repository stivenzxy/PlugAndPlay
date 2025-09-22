package com.plugAndPlay.Factories;

import com.plugAndPlay.Interfaces.AudioLoader;
import com.plugAndPlay.Interfaces.AudioRecorder;
import com.plugAndPlay.UseCases.LoadAudio;
import com.plugAndPlay.UseCases.RecordAudio;

import java.io.File;

public class AudioSourceFactory {
    private static final String AUDIO_DIRECTORY = "Audio";
    private static final String SESSION_FILENAME = "session_audio.wav";

    String sessionFilePath = AUDIO_DIRECTORY + File.separator + SESSION_FILENAME;

    public AudioRecorder createAudioRecorder() {
        return new RecordAudio(sessionFilePath);
    }

    public AudioLoader createAudioLoader() {
        return new LoadAudio(sessionFilePath);
    }
}
