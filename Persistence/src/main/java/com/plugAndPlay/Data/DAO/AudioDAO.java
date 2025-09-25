package com.plugAndPlay.Data.DAO;

import com.plugAndPlay.Entities.Audio;
import com.plugAndPlay.Entities.AudioData;
import com.plugAndPlay.Data.Config.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AudioDAO {
    private static final Logger logger = LoggerFactory.getLogger(AudioDAO.class);

    public Audio insertAudio(String name, String format, byte[] content) {
        String sql = "INSERT INTO audios(name, format, content) VALUES (?,?,?)";

        try (Connection connection = ConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, format);
            pstmt.setBytes(3, content);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                return new Audio(id, name, format);
            } else {
            throw new SQLException("No se pudo obtener el id generado.");
            }
        } catch (SQLException exception) {
            logger.error("Error al insertar el audio: {}", exception.getMessage());
            return null;
        }
    }

    public List<Audio> getAllAudios() {
        String sql = "SELECT id, name, format FROM audios";
        List<Audio> audios = new ArrayList<>();

        try (Connection connection = ConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String format = rs.getString("format");
                audios.add(new Audio(id, name, format));
            }

        } catch (SQLException exception) {
            logger.error("Error al obtener los audios: {}", exception.getMessage());
        }
        return audios;
    }

    public AudioData getAudioDataById(Long id) {
        String sql = "SELECT content FROM audios WHERE id = ?";

        try (Connection connection = ConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] content = rs.getBytes("content");
                return new AudioData(id, content);
            }

        } catch (SQLException exception) {
            logger.error("Error al obtener el contenido del audio con id {}: {}", id, exception.getMessage());
        }
        return null;
    }
}
