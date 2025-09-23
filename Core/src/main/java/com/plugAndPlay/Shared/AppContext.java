package com.plugAndPlay.Shared;

import java.util.function.Consumer;

public class AppContext {
    private String inputPath;
    private Consumer<String> uiLogger = (message) -> {};

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public Consumer<String> getUiLogger() {
        return uiLogger;
    }

    public void setUiLogger(Consumer<String> uiLogger) {
        this.uiLogger = uiLogger;
    }
}