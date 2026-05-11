package com.example.ondas_be.application.exception;

/**
 * Thrown when synced lyrics validation fails:
 * - line_index not sequential
 * - overlapping time ranges
 * - endMs <= startMs (when endMs provided)
 */
public class SyncedLyricsValidationException extends RuntimeException {

    public SyncedLyricsValidationException(String message) {
        super(message);
    }
}
