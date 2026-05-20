package com.example.ondas_be.application.exception;

public class SystemPlaylistSongNotFoundException extends RuntimeException {

    public SystemPlaylistSongNotFoundException(String message) {
        super(message);
    }
}
