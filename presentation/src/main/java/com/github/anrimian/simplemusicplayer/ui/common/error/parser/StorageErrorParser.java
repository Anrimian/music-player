package com.github.anrimian.simplemusicplayer.ui.common.error.parser;

import android.content.Context;
import android.support.annotation.StringRes;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;

/**
 * Created on 29.10.2017.
 */

public class StorageErrorParser implements ErrorParser {

    private Context context;
    private ErrorParser defaultErrorParser;

    public StorageErrorParser(Context context, ErrorParser defaultErrorParser) {
        this.context = context;
        this.defaultErrorParser = defaultErrorParser;
    }

    @Override
    public ErrorCommand parseError(Throwable throwable) {
        if (throwable instanceof FileNodeNotFoundException) {
            return new ErrorCommand(getString(R.string.file_not_found));
        }
        return defaultErrorParser.parseError(throwable);
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
