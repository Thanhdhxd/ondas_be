package com.example.ondas_be.application.exception;

public class LyricsNotFoundException extends RuntimeException {
    public LyricsNotFoundException(String message) {
        super(message);
    }
}
