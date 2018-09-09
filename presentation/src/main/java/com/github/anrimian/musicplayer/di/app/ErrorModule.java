package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParserImpl;
import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.ui.common.error.parser.DefaultErrorParser;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.common.error.parser.StorageErrorParser;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 29.10.2017.
 */

@Module
public class ErrorModule {

    public static final String STORAGE_ERROR_PARSER = "storage_error_parser";

    @Provides
    @Nonnull
    @Singleton
    ErrorParser provideErrorParser(Context context, Analytics analytics) {
        return new DefaultErrorParser(context, analytics);
    }

    @Provides
    @Nonnull
    @Singleton
    @Named(STORAGE_ERROR_PARSER)
    ErrorParser provideStorageErrorParser(Context context, ErrorParser defaultErrorParser) {
        return new StorageErrorParser(context, defaultErrorParser);
    }

    @Provides
    @Nonnull
    @Singleton
    PlayerErrorParser playerErrorParser(Analytics analytics) {
        return new PlayerErrorParserImpl(analytics);
    }
}
