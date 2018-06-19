package com.github.anrimian.simplemusicplayer.data.controllers.music.error;

import com.github.anrimian.simplemusicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.simplemusicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.simplemusicplayer.domain.models.error.ErrorType;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.FileNotFoundException;

public class PlayerErrorParserImpl implements PlayerErrorParser {

    private final Analytics analytics;

    public PlayerErrorParserImpl(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    public ErrorType getErrorType(Throwable throwable) {
        if (throwable instanceof FileDataSource.FileDataSourceException) {
            throwable = throwable.getCause();
            if (throwable instanceof FileNotFoundException) {
                return ErrorType.DELETED;
            }
        }
        analytics.processNonFatalError(throwable);
        return ErrorType.UNKNOWN;
    }
}
