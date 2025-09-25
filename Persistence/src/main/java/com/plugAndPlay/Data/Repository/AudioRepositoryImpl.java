package com.plugAndPlay.Data.Repository;

import com.plugAndPlay.Entities.Audio;
import com.plugAndPlay.Entities.AudioData;
import com.plugAndPlay.Data.DAO.AudioDAO;

import java.util.List;

public class AudioRepositoryImpl implements AudioRepository {
    private final AudioDAO audioDAO = new AudioDAO();

    @Override
    public Audio save(String name, String format, byte[] content) {
        return audioDAO.insertAudio(name, format, content);
    }

    @Override
    public List<Audio> findAll() {
        return audioDAO.getAllAudios();
    }

    @Override
    public AudioData findContentById(Long id) {
        return audioDAO.getAudioDataById(id);
    }
}
