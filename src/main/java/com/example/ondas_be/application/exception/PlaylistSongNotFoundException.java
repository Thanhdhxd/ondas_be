package com.example.ondas_be.application.exception;

public class PlaylistSongNotFoundException extends RuntimeException {

    public PlaylistSongNotFoundException(String message) {
        super(message);
    }
}
