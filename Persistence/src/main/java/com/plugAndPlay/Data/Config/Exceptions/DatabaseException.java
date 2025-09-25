package com.plugAndPlay.Data.Config.Exceptions;

public class DatabaseException extends RuntimeException {
    private final int statusCode;

    public DatabaseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}