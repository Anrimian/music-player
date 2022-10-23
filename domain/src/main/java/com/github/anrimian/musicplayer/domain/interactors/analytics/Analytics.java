package com.github.anrimian.musicplayer.domain.interactors.analytics;

import javax.annotation.Nullable;

public interface Analytics {

    //add optional message?
    void processNonFatalError(Throwable throwable);

    void processNonFatalError(Throwable throwable, @Nullable String message);

    void logMessage(String message);
}
