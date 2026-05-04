package com.example.ondas_be.application.exception;

public class FavoriteAlreadyExistsException extends RuntimeException {

    public FavoriteAlreadyExistsException(String message) {
        super(message);
    }
}
