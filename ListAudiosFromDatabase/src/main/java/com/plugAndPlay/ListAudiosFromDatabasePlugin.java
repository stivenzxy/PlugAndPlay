package com.plugAndPlay;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Data.Repository.AudioRepositoryImpl;
import com.plugAndPlay.Entities.Audio;
import com.plugAndPlay.Entities.AudioData;
import com.plugAndPlay.Interfaces.AudioListListener;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Interfaces.SelectedAudioProvider;
import com.plugAndPlay.Shared.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class ListAudiosFromDatabasePlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(ListAudiosFromDatabasePlugin.class);

    @Override
    public String getName() {
        return "Cargar Listado de audios desde BD";
    }

    @Override
    public void execute(AppContext context) {
        Consumer<String> uiLogger = context.getUiLogger();
        AudioRepository repository = context.getService(AudioRepository.class);
        if (repository == null) {
            uiLogger.accept("Error: No se encontró el servicio AudioRepository en el contexto.");
            return;
        }

        // Obtener lista
        List<Audio> audioList = getAudioList(repository);

        // Intentar entregar la lista a un listener (si existe)
        AudioListListener listListener = context.getService(AudioListListener.class);
        if (listListener != null) {
            try {
                listListener.onAudioList(audioList);
            } catch (Exception ex) {
                logger.error("Error notificando AudioListListener", ex);
                uiLogger.accept("Error: al notificar la lista de audios: " + ex.getMessage());
            }
        } else {
            uiLogger.accept("========== LISTA DE AUDIOS DISPONIBLES ==========");
            for (Audio a : audioList) {
                uiLogger.accept(">> ID: " + a.getId() + ", Nombre: " + a.getName() + ", Formato: " + a.getFormat());
            }
            uiLogger.accept("===============================================");
        }

        SelectedAudioProvider selectedProvider = context.getService(SelectedAudioProvider.class);
        if (selectedProvider != null) {
            Long selectedId = selectedProvider.getSelectedAudioId();
            if (selectedId != null) {
                uiLogger.accept("Iniciando carga del audio seleccionado (ID " + selectedId + ") ...");
                boolean ok = loadAudioToSessionFile(repository, selectedId, "Audio" + File.separator + "session_audio.wav");
                if (ok) {
                    uiLogger.accept("Éxito: audio cargado en 'Audio/session_audio.wav'");
                } else {
                    uiLogger.accept("Error: No se pudo cargar audio con ID " + selectedId);
                }
            }
        }
    }

    public List<Audio> getAudioList(AudioRepository repository) {
        if (repository == null) {
            logger.error("AudioRepository es nulo.");
            return Collections.emptyList();
        }
        return repository.findAll();
    }

    public boolean loadAudioToSessionFile(AudioRepository repository, Long audioId, String sessionFilePath) {
        if (repository == null || audioId == null) {
            logger.error("Repositorio o Audio ID nulos.");
            return false;
        }

        logger.info("Intentando cargar audio con ID {} a la sesión.", audioId);
        AudioData audioData = repository.findContentById(audioId);

        if (audioData == null || audioData.getContent() == null) {
            logger.warn("No se encontró contenido para el audio con ID: {}", audioId);
            return false;
        }

        try {
            saveBytesToSessionFile(audioData.getContent(), sessionFilePath);
            return true;
        } catch (IOException e) {
            logger.error("Error al escribir el archivo de sesión desde la BD para el audio ID {}", audioId, e);
            return false;
        }
    }

    private void saveBytesToSessionFile(byte[] content, String sessionFilePath) throws IOException {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        AudioInputStream audioStream = new AudioInputStream(
                new ByteArrayInputStream(content),
                format,
                content.length / format.getFrameSize()
        );
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, new File(sessionFilePath));
        logger.info("Bytes de audio escritos correctamente en {}", sessionFilePath);
    }


    public static void main(String[] args) {
        System.out.println("--- Ejecutando plugin 'Listar y Cargar desde BD' en modo consola ---");

        AudioRepository repository = new AudioRepositoryImpl();
        ListAudiosFromDatabasePlugin plugin = new ListAudiosFromDatabasePlugin();

        List<Audio> audioList = plugin.getAudioList(repository);
        if (audioList.isEmpty()) {
            System.out.println("No se encontraron audios en la base de datos.");
            return;
        }

        System.out.println("\n--- Audios Disponibles en la Base de Datos ---");
        for (Audio audio : audioList) {
            System.out.printf("  ID: %d, Nombre: %s, Formato: %s\n", audio.getId(), audio.getName(), audio.getFormat());
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("\n> Ingrese el ID del audio que desea cargar a la sesión: ");
            long selectedId = scanner.nextLong();

            boolean success = plugin.loadAudioToSessionFile(repository, selectedId, "Audio" + File.separator + "session_audio.wav");

            if (success) {
                System.out.println("\nÉxito: El audio con ID " + selectedId + " ha sido guardado en 'Audio/session_audio.wav'");
            } else {
                System.err.println("\nError: No se pudo cargar el audio con ID " + selectedId);
            }
        } catch (Exception e) {
            System.err.println("\nError: Entrada inválida. Por favor, ingrese un número de ID válido.");
        }
    }
}