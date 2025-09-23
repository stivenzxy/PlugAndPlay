package com.plugAndPlay.Actions;

import com.plugAndPlay.Interfaces.AudioLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.function.Consumer;

public class LoadAudioAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(LoadAudioAction.class);

    private final JFrame parentFrame;
    private final AudioLoader loadAudioUseCase;
    private final Consumer<String> uiLogger;

    public LoadAudioAction(JFrame parentFrame, AudioLoader loadAudioUseCase, Consumer<String> uiLogger) {
        super("Cargar Audio");
        this.parentFrame = parentFrame;
        this.loadAudioUseCase = loadAudioUseCase;
        this.uiLogger = uiLogger;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de audio");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Audio", "wav", "mp3", "au"));

        if (fileChooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                File loadedFile = loadAudioUseCase.execute(selectedFile);

                logger.info("Caso de uso 'LoadAudio' ejecutado con éxito para el archivo: {}", selectedFile.getName());
                uiLogger.accept(">> Archivo guardado: " + loadedFile);

            } catch (Exception ex) {
                logger.error("El caso de uso 'LoadAudio' falló.", ex);
                uiLogger.accept(">> Error al cargar el archivo: " + ex.getMessage());
            }
        }
    }
}
