package com.example.ondas_be.application.exception;

public class SystemPlaylistNotFoundException extends RuntimeException {

    public SystemPlaylistNotFoundException(String message) {
        super(message);
    }
}
