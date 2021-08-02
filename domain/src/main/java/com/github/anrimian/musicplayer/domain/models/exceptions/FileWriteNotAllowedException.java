package com.github.anrimian.musicplayer.domain.models.exceptions;

public class FileWriteNotAllowedException extends RuntimeException {

    public FileWriteNotAllowedException(String message) {
        super(message);
    }
}
