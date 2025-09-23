package com.plugAndPlay.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EnvironmentInitializer {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentInitializer.class);

    public static void initEnvironment() {
        try {
            Path audioDirPath = Paths.get("Audio");
            Files.createDirectories(audioDirPath);
            logger.info("Directorio 'Audio' inicializado correctamente en: {}", audioDirPath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("No se pudo crear el directorio 'Audio'", e);
        }
    }
}
