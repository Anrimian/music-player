package com.github.anrimian.simplemusicplayer.domain.utils.validation;

public class ValidateError {

    private Cause cause;

    public ValidateError(Cause cause) {
        this.cause = cause;
    }

    public Cause getCause() {
        return cause;
    }
}
