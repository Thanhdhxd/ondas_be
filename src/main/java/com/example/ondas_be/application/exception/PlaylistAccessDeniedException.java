package com.example.ondas_be.application.exception;

public class PlaylistAccessDeniedException extends RuntimeException {

    public PlaylistAccessDeniedException(String message) {
        super(message);
    }
}
