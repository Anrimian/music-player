package com.github.anrimian.simplemusicplayer;

import com.github.anrimian.simplemusicplayer.domain.business.analytics.Analytics;

public class AnalyticsImpl implements Analytics {

    @Override
    public void processNonFatalError(Throwable throwable) {
        throwable.printStackTrace();
        //        if (Fabric.isInitialized()) {//TODO debug/release versions
//            if (Crashlytics.getInstance() != null) {
//                Crashlytics.logException(throwable);
//            }
//        }
    }
}
