package com.example.ondas_be.application.exception;

public class PlaylistNotFoundException extends RuntimeException {

    public PlaylistNotFoundException(String message) {
        super(message);
    }
}
