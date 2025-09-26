package com.plugAndPlay.Facade;

import com.plugAndPlay.Interfaces.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginManager {
    private final static Logger logger = LoggerFactory.getLogger(PluginManager.class);
    private final List<Plugin> loadedPlugins = new ArrayList<>();
    private java.util.function.Consumer<String> uiLogger = (msg) -> {};

    public void loadPlugin(File jarFile) {
        try {
            logger.info("Iniciando carga de plugin desde: " + jarFile.getAbsolutePath());
            
            URL[] urls = {jarFile.toURI().toURL()};
            URLClassLoader pluginClassLoader = new URLClassLoader(urls, getClass().getClassLoader());

            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(pluginClassLoader);

                ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, pluginClassLoader);
                serviceLoader.reload();

                int count = 0;
                for (Plugin plugin : serviceLoader) {
                    String successMsg = "Plugin cargado: " + plugin.getName();
                    logger.info(successMsg);
                    uiLogger.accept(">> " + successMsg);
                    loadedPlugins.add(plugin);
                    count++;
                }
                if (count == 0) {
                    String warnMsg = "No se encontraron plugins válidos en " + jarFile.getName();
                    logger.warn(warnMsg);
                    uiLogger.accept(">> " + warnMsg);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(previous);
            }
        } catch (Exception e) {
            String errorMsg = "Error al intentar cargar el Plugin: " + e.getMessage();
            logger.error(errorMsg, e);
            uiLogger.accept(">> " + errorMsg);
        }
    }

    public List<Plugin> getLoadedPlugins() {
        return loadedPlugins;
    }
    
    public void setUiLogger(java.util.function.Consumer<String> uiLogger) {
        this.uiLogger = uiLogger;
    }
}
