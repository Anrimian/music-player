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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorEvent that = (ErrorEvent) o;

        return errorType == that.errorType;
    }

    @Override
    public int hashCode() {
        return errorType.hashCode();
    }
}
