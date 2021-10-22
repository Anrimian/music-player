package com.github.anrimian.musicplayer.data.controllers.music.error;

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.FileNotFoundException;

public class PlayerErrorParserImpl implements PlayerErrorParser {

    private final Analytics analytics;

    public PlayerErrorParserImpl(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    public ErrorType getErrorType(Throwable throwable) {
        if (throwable instanceof FileNotFoundException) {
            return ErrorType.NOT_FOUND;
        }
        if (throwable instanceof PlaybackException) {
            Throwable cause = throwable.getCause();
            if (cause instanceof ContentDataSource.ContentDataSourceException
                || cause instanceof FileDataSource.FileDataSourceException) {
                Throwable causeOfCause = cause.getCause();
                if (causeOfCause instanceof FileNotFoundException) {
                    return ErrorType.NOT_FOUND;
                }
            }
            if (cause instanceof UnrecognizedInputFormatException) {
                return ErrorType.UNSUPPORTED;
            }
        }
        if (throwable instanceof SecurityException) {
            return ErrorType.IGNORED;
        }
        analytics.processNonFatalError(throwable);
        return ErrorType.UNKNOWN;
    }
}
