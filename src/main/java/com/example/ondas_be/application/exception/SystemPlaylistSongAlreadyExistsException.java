package com.example.ondas_be.application.exception;

public class SystemPlaylistSongAlreadyExistsException extends RuntimeException {

    public SystemPlaylistSongAlreadyExistsException(String message) {
        super(message);
    }
}
