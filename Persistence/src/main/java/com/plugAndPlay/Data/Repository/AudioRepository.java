package com.plugAndPlay.Data.Repository;

import com.plugAndPlay.Entities.Audio;
import com.plugAndPlay.Entities.AudioData;

import java.util.List;

public interface AudioRepository {
    Audio save(String name, String format, byte[] content);
    List<Audio> findAll();
    AudioData findContentById(Long id);
}
