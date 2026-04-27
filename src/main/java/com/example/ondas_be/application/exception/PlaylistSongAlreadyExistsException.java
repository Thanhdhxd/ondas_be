package com.example.ondas_be.application.exception;

public class PlaylistSongAlreadyExistsException extends RuntimeException {

    public PlaylistSongAlreadyExistsException(String message) {
        super(message);
    }
}
