package com.github.anrimian.musicplayer.domain.utils.validation;

public class ValidateError {

    private final Cause cause;

    public ValidateError(Cause cause) {
        this.cause = cause;
    }

    public Cause getCause() {
        return cause;
    }
}
