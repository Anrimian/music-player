package com.github.anrimian.musicplayer.domain.models.player.events;

import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;

public class ErrorEvent implements PlayerEvent {

    private ErrorType errorType;

    public ErrorEvent(ErrorType errorType) {
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
