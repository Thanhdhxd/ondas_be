package com.example.ondas_be.application.exception;

/**
 * Thrown when attempting to create lyrics for a song that already has lyrics.
 * Lyrics have a 1-to-1 relationship with songs.
 */
public class LyricsAlreadyExistsException extends RuntimeException {

    public LyricsAlreadyExistsException(String message) {
        super(message);
    }
}
