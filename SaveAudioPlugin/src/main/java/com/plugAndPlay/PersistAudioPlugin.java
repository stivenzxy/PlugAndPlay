package com.plugAndPlay;

import com.plugAndPlay.Data.Repository.AudioRepository;
import com.plugAndPlay.Data.Repository.AudioRepositoryImpl;
import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PersistAudioPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(PersistAudioPlugin.class);

    @Override
    public String getName() {
        return "Persistir Audio en BD";
    }

    @Override
    public void execute(AppContext context) {
        String audioFilePath = context.getInputPath();
        var uiLogger = context.getUiLogger();

        AudioRepository repository = context.getService(AudioRepository.class);

        if (repository == null) {
            logger.error("El servicio AudioRepository no fue inyectado en el contexto.");
            uiLogger.accept(">> Error: El servicio de base de datos no está disponible.");
            return;
        }
        if (audioFilePath == null || audioFilePath.isEmpty()) {
            logger.warn("No se proporcionó una ruta de archivo de entrada.");
            uiLogger.accept(">> Error: No hay un archivo de sesión activo para persistir.");
            return;
        }

        try {
            File audioFile = new File(audioFilePath);
            logger.info("Leyendo archivo de sesión para persistencia: {}", audioFile.getAbsolutePath());

            byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

            logger.info("Llamando al repositorio para guardar {} bytes.", audioBytes.length);
            repository.save(audioFile.getName(), "wav", audioBytes);

            uiLogger.accept(">> Audio '" + audioFile.getName() + "' almacenado en la base de datos con éxito.");

        } catch (IOException e) {
            logger.error("Error al leer el archivo de sesión: {}", audioFilePath, e);
            uiLogger.accept(">> Error: No se pudo leer el archivo de audio para guardarlo.");
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java com.plugAndPlay.Plugins.PersistAudioPlugin <ruta_al_archivo.wav>");
            return;
        }

        AppContext consoleContext = new AppContext();
        consoleContext.setInputPath(args[0]);

        AudioRepository repository = new AudioRepositoryImpl();
        consoleContext.registerService(AudioRepository.class, repository);

        PersistAudioPlugin plugin = new PersistAudioPlugin();
        plugin.execute(consoleContext);
    }
}
