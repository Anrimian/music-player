package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.ui.common.error.parser.DefaultErrorParser;
import com.github.anrimian.simplemusicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.simplemusicplayer.ui.common.error.parser.StorageErrorParser;

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
    ErrorParser provideErrorParser(Context context) {
        return new DefaultErrorParser(context);
    }

    @Provides
    @Nonnull
    @Singleton
    @Named(STORAGE_ERROR_PARSER)
    ErrorParser provideStorageErrorParser(Context context, ErrorParser defaultErrorParser) {
        return new StorageErrorParser(context, defaultErrorParser);
    }
}
