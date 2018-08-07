package com.github.anrimian.simplemusicplayer.ui.common.error.parser;

import android.content.Context;
import android.support.annotation.StringRes;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.data.models.exceptions.PlayListNotCreatedException;
import com.github.anrimian.simplemusicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.simplemusicplayer.domain.utils.validation.ValidateError;
import com.github.anrimian.simplemusicplayer.domain.utils.validation.ValidateException;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;

import java.util.List;

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
        if (throwable instanceof ValidateException) {
            ValidateException exception = (ValidateException) throwable;
            List<ValidateError> validateErrors = exception.getValidateErrors();
            for (ValidateError validateError: validateErrors) {
                switch (validateError.getCause()) {
                    case EMPTY_NAME: {
                        return new ErrorCommand(getString(R.string.name_can_not_be_empty));
                    }
                }
            }
        }
        if (throwable instanceof PlayListNotCreatedException) {
            return new ErrorCommand(getString(R.string.play_list_with_this_name_already_exists));
        }
        if (throwable instanceof NullPointerException) {
            logException(throwable);
            return new ErrorCommand(getString(R.string.internal_app_error));
        }
        logException(throwable);
        return new ErrorCommand(getString(R.string.unexpected_error));
    }

    private void logException(Throwable throwable) {
        analytics.processNonFatalError(throwable);
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
