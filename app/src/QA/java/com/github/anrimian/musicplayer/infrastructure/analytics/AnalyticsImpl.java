package com.github.anrimian.musicplayer.infrastructure.analytics;

import android.util.Log;

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.utils.logger.FileLog;

import javax.annotation.Nullable;

public class AnalyticsImpl implements Analytics {

    private final FileLog fileLog;

    public AnalyticsImpl(FileLog fileLog) {
        this.fileLog = fileLog;
    }

    public void processNonFatalError(Throwable throwable) {
        processNonFatalError(throwable, null);
    }

    @Override
    public void processNonFatalError(Throwable throwable, @Nullable String message) {
        if (message != null) {
            Log.d("UNEXPECTED", message);
        }
        throwable.printStackTrace();
        fileLog.writeException(throwable, message);
    }

    @Override
    public void logMessage(String message) {
        Log.d("UNEXPECTED", message);
        fileLog.writeMessage(message);
    }
}
