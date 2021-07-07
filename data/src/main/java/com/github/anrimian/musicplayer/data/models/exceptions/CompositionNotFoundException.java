package com.github.anrimian.musicplayer.data.models.exceptions;

/**
 * Created on 16.04.2018.
 */
public class CompositionNotFoundException extends RuntimeException {

    public CompositionNotFoundException(String message) {
        super(message);
    }
}
