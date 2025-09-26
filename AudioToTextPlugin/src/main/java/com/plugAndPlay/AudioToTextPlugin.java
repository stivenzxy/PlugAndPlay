package com.plugAndPlay;

import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AudioToTextPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(AudioToTextPlugin.class);
    private static final String VOSK_SERVER_URL = "ws://localhost:2700";
    private static final int SAMPLE_RATE = 16000;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String getName() {
        return "Extraer Texto del Audio";
    }

    @Override
    public void execute(AppContext context) {
        String audioFilePath = context.getInputPath();
        var uiLogger = context.getUiLogger();

        if (audioFilePath == null || audioFilePath.isEmpty()) {
            logger.error("No se especificó una ruta de archivo de audio en el contexto.");
            uiLogger.accept(">> Error: No se ha cargado ningún audio para procesar.");
            return;
        }

        uiLogger.accept(">> Iniciando extracción de texto de " + new File(audioFilePath).getName() + "...");

        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            logger.error("Archivo de audio no encontrado: {}", audioFilePath);
            uiLogger.accept(">> Error: El archivo de audio no se encontró en el disco.");
            return;
        }

        try {
            if (!isVoskServerAvailable()) {
                uiLogger.accept(">> Error: El servidor Vosk no está disponible. Asegúrese de que esté ejecutándose en " + VOSK_SERVER_URL);
                logger.error("Servidor Vosk no disponible en {}", VOSK_SERVER_URL);
                return;
            }

            uiLogger.accept(">> Convirtiendo audio al formato requerido...");
            File convertedAudio = convertAudioForVosk(audioFile);

            int timeoutSeconds = 120;
            double durationSec = 0;
            try (AudioInputStream durStream = AudioSystem.getAudioInputStream(convertedAudio)) {
                long frames = durStream.getFrameLength();
                float frameRate = durStream.getFormat().getFrameRate();
                if (frames > 0 && frameRate > 0) {
                    durationSec = frames / frameRate;
                    timeoutSeconds = Math.max(180, (int) Math.ceil(durationSec * 8.0 + 120));
                    uiLogger.accept(String.format(">> Audio de %.1f segundos detectado. Timeout: %d segundos", durationSec, timeoutSeconds));
                }
            }

            uiLogger.accept(">> Pasando de audio a texto... Por favor espere...");
            String extractedText = processAudioWithVosk(convertedAudio, timeoutSeconds);
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                String textDirPath = "Texto";
                String textFilePath = textDirPath + "/session_text.txt";
                
                File textDir = new File(textDirPath);
                if (!textDir.exists()) {
                    textDir.mkdirs();
                }
                
                saveTextToFile(extractedText, textFilePath);
                
                uiLogger.accept(">> Texto extraído exitosamente:");
                uiLogger.accept(">> " + extractedText);
                uiLogger.accept(">> Texto guardado en: " + textFilePath);
            } else {
                uiLogger.accept(">> No se pudo extraer texto del audio o el audio no contiene palabras reconocibles.");
                logger.warn("No se extrajo texto del archivo: {}", audioFilePath);
            }
            
            if (!convertedAudio.equals(audioFile)) {
                convertedAudio.delete();
            }
            
        } catch (Exception e) {
            logger.error("Error crítico durante la extracción de texto del audio.", e);
            uiLogger.accept(">> Error al procesar el audio: " + e.getMessage());
        }
    }

    private boolean isVoskServerAvailable() {
        try {
            URI serverUri = new URI(VOSK_SERVER_URL);
            CompletableFuture<Boolean> connectionResult = new CompletableFuture<>();
            
            WebSocketClient testClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connectionResult.complete(true);
                    this.close();
                }

                @Override
                public void onMessage(String message) {}

                @Override
                public void onClose(int code, String reason, boolean remote) {}

                @Override
                public void onError(Exception ex) {
                    connectionResult.complete(false);
                }
            };
            
            testClient.connect();
            return connectionResult.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error verificando disponibilidad del servidor Vosk", e);
            return false;
        }
    }

    private File convertAudioForVosk(File inputFile) throws Exception {
        AudioInputStream originalStream = AudioSystem.getAudioInputStream(inputFile);
        AudioFormat originalFormat = originalStream.getFormat();
        
        AudioFormat targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            16,
            1,
            2,
            SAMPLE_RATE,
            false
        );
        
        if (isFormatCompatible(originalFormat, targetFormat)) {
            return inputFile;
        }
        
        AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);
        
        File tempFile = File.createTempFile("vosk_audio", ".wav");
        tempFile.deleteOnExit();
        
        AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, tempFile);
        
        originalStream.close();
        convertedStream.close();
        
        return tempFile;
    }

    private boolean isFormatCompatible(AudioFormat current, AudioFormat target) {
        return Math.abs(current.getSampleRate() - target.getSampleRate()) < 1 &&
               current.getChannels() == target.getChannels() &&
               current.getSampleSizeInBits() == target.getSampleSizeInBits();
    }

    private String processAudioWithVosk(File audioFile, int timeoutSeconds) throws Exception {
        
        URI serverUri = new URI(VOSK_SERVER_URL);
        StringBuilder result = new StringBuilder();
        CompletableFuture<String> processingResult = new CompletableFuture<>();
        boolean[] eofSent = {false};
        long[] lastMessageTime = {System.currentTimeMillis()};
        String[] lastPartialResult = {""};
        
        WebSocketClient client = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(audioFile)) {
                    String config = "{\"config\": {\"sample_rate\": " + SAMPLE_RATE + ", \"words\": true, \"partial\": true, \"max_alternatives\": 0}}";
                    send(config);
                    
                    Thread.sleep(200);

                    byte[] buffer = new byte[2048];
                    int read;
                    int totalSent = 0;
                    int chunkCount = 0;
                    
                    while ((read = pcmStream.read(buffer)) != -1) {
                        if (read > 0) {
                            byte[] chunk = new byte[read];
                            System.arraycopy(buffer, 0, chunk, 0, read);
                            send(chunk);
                            totalSent += read;
                            
                            if (++chunkCount % 5 == 0) {
                                Thread.sleep(100);
                            }
                        }
                    }
                    
                    byte[] silence = new byte[1024];
                    for (int i = 0; i < 3; i++) {
                        send(silence);
                        Thread.sleep(100);
                    }
                    
                    Thread.sleep(1000);
                    send("{\"eof\": 1}");
                    eofSent[0] = true;
                    
                    Thread.sleep(3000);
                    
                    send("{\"flush\": true}");
                    
                    Thread.sleep(2000);
                    
                    String currentResult = result.toString().trim();
                    String finalResult = concatenatePartialResult(currentResult, lastPartialResult[0]);
                    
                    if (!finalResult.equals(currentResult)) {
                        processingResult.complete(finalResult);
                    }

                } catch (Exception e) {
                    logger.error("Error enviando audio al servidor Vosk", e);
                    processingResult.completeExceptionally(e);
                }
            }

            @Override
            public void onMessage(String message) {
                try {
                    lastMessageTime[0] = System.currentTimeMillis();
                    JsonNode jsonResponse = objectMapper.readTree(message);
                    
                    if (jsonResponse.has("text")) {
                        String text = jsonResponse.get("text").asText();
                        if (!text.trim().isEmpty()) {
                            result.append(text).append(" ");
                        }
                    }
                    
                    if (jsonResponse.has("partial")) {
                        String partialText = jsonResponse.get("partial").asText();
                        if (!partialText.trim().isEmpty()) {
                            lastPartialResult[0] = partialText;
                        }
                    }
                    
                    if (eofSent[0]) {
                        if (jsonResponse.has("text")) {
                            String finalText = jsonResponse.get("text").asText();
                            if (!finalText.trim().isEmpty()) {
                                String currentResult = result.toString();
                                if (!currentResult.contains(finalText.trim())) {
                                    result.append(finalText).append(" ");
                                }
                            }
                        }
                        
                        if (System.currentTimeMillis() - lastMessageTime[0] > 5000) {
                            if (!processingResult.isDone()) {
                                String confirmedResult = result.toString().trim();
                                String finalResult = concatenatePartialResult(confirmedResult, lastPartialResult[0]);
                                processingResult.complete(finalResult);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error procesando respuesta del servidor Vosk: {}", message, e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (!processingResult.isDone()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    String confirmedResult = result.toString().trim();
                    String finalResult = concatenatePartialResult(confirmedResult, lastPartialResult[0]);
                    
                    processingResult.complete(finalResult);
                }
            }

            @Override
            public void onError(Exception ex) {
                logger.error("Error en conexión WebSocket con Vosk", ex);
                processingResult.completeExceptionally(ex);
            }
        };
        
        client.connect();
        
        try {
            String finalResult = processingResult.get(timeoutSeconds, TimeUnit.SECONDS);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return finalResult;
        } finally {
            if (client.isOpen()) {
                client.close();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void saveTextToFile(String text, String outputPath) throws IOException {
        Files.write(Paths.get(outputPath), text.getBytes("UTF-8"));
    }
    
    private String concatenatePartialResult(String confirmedResult, String partialResult) {
        if (confirmedResult.isEmpty()) {
            return partialResult;
        } else if (!partialResult.isEmpty() && !confirmedResult.contains(partialResult)) {
            return confirmedResult + " " + partialResult;
        }
        return confirmedResult;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java com.plugAndPlay.AudioToTextPlugin <ruta_al_archivo.wav>");
            return;
        }

        AppContext consoleContext = new AppContext();
        consoleContext.setInputPath(args[0]);
        consoleContext.setUiLogger(System.out::println);

        AudioToTextPlugin plugin = new AudioToTextPlugin();
        plugin.execute(consoleContext);
    }
}

