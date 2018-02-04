package com.github.anrimian.simplemusicplayer.ui.common.error.parser;

import android.content.Context;
import android.support.annotation.StringRes;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;

/**
 * Created on 29.10.2017.
 */

public class DefaultErrorParser implements ErrorParser {

    private Context context;

    public DefaultErrorParser(Context context) {
        this.context = context;
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

    protected void logException(Throwable throwable) {
        throwable.printStackTrace();
//        if (Fabric.isInitialized()) {//TODO debug/release versions
//            if (Crashlytics.getInstance() != null) {
//                Crashlytics.logException(throwable);
//            }
//        }
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
