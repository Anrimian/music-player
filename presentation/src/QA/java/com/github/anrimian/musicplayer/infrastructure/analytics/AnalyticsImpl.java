package com.github.anrimian.musicplayer.infrastructure.analytics;

import android.util.Log;

import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;

public class AnalyticsImpl implements Analytics {

    @Override
    public void processNonFatalError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void logMessage(String message) {
        Log.d("UNEXPECTED", message);
    }
}
