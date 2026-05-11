package com.example.ondas_be.application.exception;

/**
 * Thrown when a lyrics record is not found.
 */
public class LyricsNotFoundException extends RuntimeException {

    public LyricsNotFoundException(String message) {
        super(message);
    }
}
