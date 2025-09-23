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
    private final List<Plugin> loadedPlugins = new ArrayList<Plugin>();

    public void loadPlugin(File jarFile) {
        try {
            URL[] urls = {jarFile.toURI().toURL()};
            URLClassLoader pluginClassLoader = new URLClassLoader(urls, getClass().getClassLoader());

            ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, pluginClassLoader);

            for (Plugin plugin : serviceLoader) {
                logger.info("Plugin encontrado y cargado exitosamente: {}", plugin.getName());
                loadedPlugins.add(plugin);
            }
        } catch (Exception e) {
            logger.error("Error al intentar cargar el Plugin: ", e);
        }
    }

    public List<Plugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}
