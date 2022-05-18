package com.github.anrimian.musicplayer.ui.common.error.parser;

import android.content.Context;

import androidx.annotation.StringRes;

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

/**
 * Created on 29.10.2017.
 */

public abstract class ErrorParser {

    private final Context context;

    public ErrorParser(Context context) {
        this.context = context;
    }

    protected String getString(@StringRes int resId) {
        return context.getString(resId);
    }

    protected String getString(@StringRes int resId, Object... formatArgs) {
        return context.getString(resId, formatArgs);
    }

    protected ErrorCommand error(@StringRes int resId) {
        return new ErrorCommand(getString(resId));
    }

    public abstract ErrorCommand parseError(Throwable throwable);

    public abstract void logError(Throwable throwable);
}
