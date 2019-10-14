package com.github.anrimian.musicplayer.data.storage.exceptions;

public class UpdateMediaStoreException extends RuntimeException {

    public UpdateMediaStoreException(String message) {
        super(message);
    }

    public UpdateMediaStoreException(Throwable cause) {
        super(cause);
    }
}
