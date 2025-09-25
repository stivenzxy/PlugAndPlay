package com.plugAndPlay.Entities;

public class AudioData {
    private Long audioId;
    private byte[] content;

    public AudioData(Long audioId, byte[] content) {
        this.audioId = audioId;
        this.content = content;
    }

    public Long getAudioId() { return audioId; }

    public void setAudioId(Long audioId) { this.audioId = audioId; }

    public byte[] getContent() { return content; }

    public void setContent(byte[] content) { this.content = content; }
}
