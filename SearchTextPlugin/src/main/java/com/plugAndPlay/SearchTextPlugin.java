package com.plugAndPlay;

import com.plugAndPlay.Interfaces.Plugin;
import com.plugAndPlay.Shared.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchTextPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(SearchTextPlugin.class);
    private static final String SESSION_TEXT_PATH = "Texto/session_text.txt";
    
    @Override
    public String getName() {
        return "Buscar Texto";
    }

    @Override
    public void execute(AppContext context) {
        var uiLogger = context.getUiLogger();
        
        uiLogger.accept(">> Iniciando búsqueda de texto...");
        
        String textFilePath = context.getInputPath();
        if (textFilePath == null || textFilePath.isEmpty()) {
            textFilePath = SESSION_TEXT_PATH;
        }

        File textFile = new File(textFilePath);
        if (!textFile.exists()) {
            uiLogger.accept(">> Error: No se encontró el archivo de texto: " + textFilePath);
            logger.error("Archivo de texto no encontrado: {}", textFilePath);
            return;
        }

        try {
            String content = Files.readString(Paths.get(textFilePath)).trim();
            if (content.isEmpty()) {
                uiLogger.accept(">> Error: El archivo de texto está vacío");
                logger.warn("Archivo de texto vacío: {}", textFilePath);
                return;
            }

            uiLogger.accept(">> Texto disponible para búsqueda:");
            uiLogger.accept(">> \"" + content + "\"");
            uiLogger.accept(">> Longitud: " + content.length() + " caracteres");
            
            String searchQuery = context.getSearchQuery();
            
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                if (isConsoleMode()) {
                    searchQuery = promptUserForSearchQuery();
                    
                    if (searchQuery == null || searchQuery.trim().isEmpty()) {
                        uiLogger.accept(">> Búsqueda cancelada por el usuario");
                        return;
                    }
                } else {
                    uiLogger.accept(">> Error: No se proporcionó texto para buscar");
                    uiLogger.accept(">> Use la interfaz gráfica para ingresar el texto de búsqueda");
                    return;
                }
            }
            
            searchQuery = searchQuery.trim();
            uiLogger.accept(">> Buscando: \"" + searchQuery + "\"");
            
            SearchResult result = performSearch(content, searchQuery);
            
            displaySearchResults(result, uiLogger);
            
        } catch (IOException e) {
            uiLogger.accept(">> Error al leer el archivo de texto: " + e.getMessage());
            logger.error("Error leyendo archivo de texto", e);
        } catch (Exception e) {
            uiLogger.accept(">> Error durante la búsqueda: " + e.getMessage());
            logger.error("Error crítico durante la búsqueda", e);
        }
    }

    private boolean isConsoleMode() {
        return System.getProperty("java.awt.headless", "false").equals("true") || 
               !java.awt.GraphicsEnvironment.isHeadless();
    }

    private String promptUserForSearchQuery() {
        try {
            String query = JOptionPane.showInputDialog(
                null,
                "Ingrese la palabra o frase a buscar en el texto:",
                "Búsqueda de Texto",
                JOptionPane.PLAIN_MESSAGE
            );
            return query;
        } catch (Exception e) {
            logger.error("Error al mostrar diálogo de input", e);
            return null;
        }
    }

    private SearchResult performSearch(String content, String query) {
        SearchResult result = new SearchResult(query);
        
        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();
        
        int index = 0;
        while ((index = lowerContent.indexOf(lowerQuery, index)) != -1) {
            int contextStart = Math.max(0, index - 30);
            int contextEnd = Math.min(content.length(), index + query.length() + 30);
            
            String context = content.substring(contextStart, contextEnd);
            if (contextStart > 0) context = "..." + context;
            if (contextEnd < content.length()) context = context + "...";
            
            Match match = new Match(index, context);
            result.addMatch(match);
            
            index += query.length();
        }
        
        if (result.getMatches().isEmpty()) {
            performFuzzySearch(content, query, result);
        }
        
        return result;
    }

    private void performFuzzySearch(String content, String query, SearchResult result) {
        String[] words = content.toLowerCase().split("\\s+");
        String lowerQuery = query.toLowerCase();
        
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains(lowerQuery) && !words[i].equals(lowerQuery)) {
                String originalWord = findOriginalWord(content, words[i], i);
                if (originalWord != null) {
                    String context = "Palabra similar encontrada: " + originalWord;
                    Match match = new Match(-1, context);
                    result.addSimilarMatch(match);
                }
            }
        }
    }

    private String findOriginalWord(String content, String lowercaseWord, int wordIndex) {
        String[] originalWords = content.split("\\s+");
        if (wordIndex < originalWords.length) {
            return originalWords[wordIndex];
        }
        return null;
    }

    private void displaySearchResults(SearchResult result, java.util.function.Consumer<String> uiLogger) {
        uiLogger.accept(">> ========== RESULTADOS DE BÚSQUEDA ==========");
        
        if (result.getMatches().isEmpty() && result.getSimilarMatches().isEmpty()) {
            uiLogger.accept(">> No se encontraron coincidencias para: \"" + result.getQuery() + "\"");
            uiLogger.accept(">> Sugerencias:");
            uiLogger.accept(">>   - Verifique la ortografía");
            uiLogger.accept(">>   - Intente con una palabra más corta");
            uiLogger.accept(">>   - Use solo parte de la frase");
        } else {
            if (!result.getMatches().isEmpty()) {
                uiLogger.accept(">> COINCIDENCIAS EXACTAS (" + result.getMatches().size() + "):");
                for (int i = 0; i < result.getMatches().size(); i++) {
                    Match match = result.getMatches().get(i);
                    uiLogger.accept(">>   " + (i + 1) + ". Posición " + match.getPosition() + ": " + match.getContext());
                }
            }

            if (!result.getSimilarMatches().isEmpty()) {
                uiLogger.accept(">> COINCIDENCIAS SIMILARES (" + result.getSimilarMatches().size() + "):");
                for (int i = 0; i < result.getSimilarMatches().size(); i++) {
                    Match match = result.getSimilarMatches().get(i);
                    uiLogger.accept(">>   " + (i + 1) + ". " + match.getContext());
                }
            }
        }
        
        uiLogger.accept(">> ==========================================");
    }

    private static class SearchResult {
        private final String query;
        private final List<Match> matches = new ArrayList<>();
        private final List<Match> similarMatches = new ArrayList<>();
        
        public SearchResult(String query) {
            this.query = query;
        }
        
        public String getQuery() { return query; }
        public List<Match> getMatches() { return matches; }
        public List<Match> getSimilarMatches() { return similarMatches; }
        
        public void addMatch(Match match) { matches.add(match); }
        public void addSimilarMatch(Match match) { similarMatches.add(match); }
    }
    
    private static class Match {
        private final int position;
        private final String context;
        
        public Match(int position, String context) {
            this.position = position;
            this.context = context;
        }
        
        public int getPosition() { return position; }
        public String getContext() { return context; }
    }

    public static void main(String[] args) {
        AppContext consoleContext = new AppContext();
        consoleContext.setUiLogger(System.out::println);

        if (args.length > 0) {
            consoleContext.setInputPath(args[0]);
            System.out.println(">> Usando archivo de texto: " + args[0]);
            
            if (args.length > 1) {
                consoleContext.setSearchQuery(args[1]);
                System.out.println(">> Buscando: \"" + args[1] + "\"");
            }
        } else {
            System.out.println(">> Usando archivo de texto por defecto: " + SESSION_TEXT_PATH);
            System.out.println(">> Uso: java -jar SearchTextPlugin.jar [ruta_al_archivo.txt] [texto_a_buscar]");
        }

        SearchTextPlugin plugin = new SearchTextPlugin();
        plugin.execute(consoleContext);
    }
}
