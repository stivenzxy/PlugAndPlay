package com.plugAndPlay.Shared;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AppContext {
    private String inputPath;
    private String searchQuery;
    private Consumer<String> uiLogger = (msg) -> {};

    private final Map<Class<?>, Object> services = new HashMap<>();

    public String getInputPath() { return inputPath; }
    public void setInputPath(String inputPath) { this.inputPath = inputPath; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    public Consumer<String> getUiLogger() { return uiLogger; }
    public void setUiLogger(Consumer<String> uiLogger) { this.uiLogger = uiLogger; }


    public <T> void registerService(Class<T> serviceInterface, T serviceImplementation) {
        services.put(serviceInterface, serviceImplementation);
    }

    public <T> T getService(Class<T> serviceInterface) {
        Object service = services.get(serviceInterface);
        if (serviceInterface.isInstance(service)) {
            return serviceInterface.cast(service);
        }
        return null;
    }
}