package com.github.anrimian.simplemusicplayer.utils.error;

import javax.annotation.Nonnull;

/**
 * Created on 29.10.2017.
 */

public class ErrorCommand {

    @Nonnull
    private String message;

    public ErrorCommand(@Nonnull String message) {
        this.message = message;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }
}
