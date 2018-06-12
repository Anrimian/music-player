package com.github.anrimian.simplemusicplayer.ui.common.error.parser;

import android.content.Context;
import android.support.annotation.StringRes;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;

/**
 * Created on 29.10.2017.
 */

public class DefaultErrorParser implements ErrorParser {

    private final Context context;
    private final Analytics analytics;

    public DefaultErrorParser(Context context, Analytics analytics) {
        this.context = context;
        this.analytics = analytics;
    }

    @Override
    public ErrorCommand parseError(Throwable throwable) {
        if (throwable instanceof NullPointerException) {
            logException(throwable);
            return new ErrorCommand(getString(R.string.internal_app_error));
        }
        logException(throwable);
        return new ErrorCommand(getString(R.string.unexpected_error));
    }

    public void logException(Throwable throwable) {
        analytics.processNonFatalError(throwable);
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
