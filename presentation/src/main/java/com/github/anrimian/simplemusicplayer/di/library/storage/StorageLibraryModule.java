package com.github.anrimian.simplemusicplayer.di.library.storage;

import com.github.anrimian.simplemusicplayer.domain.business.music.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.ui.library.storage.StorageLibraryPresenter;
import com.github.anrimian.simplemusicplayer.utils.error.parser.ErrorParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

import static com.github.anrimian.simplemusicplayer.di.app.ErrorModule.STORAGE_ERROR_PARSER;

/**
 * Created on 31.10.2017.
 */

@Module
public class StorageLibraryModule {

    @Nullable
    private String path;

    public StorageLibraryModule(@Nullable String path) {
        this.path = path;
    }

    @Provides
    @Nonnull
    StorageLibraryPresenter provideStorageLibraryPresenter(StorageLibraryInteractor interactor,
                                                           @Named(STORAGE_ERROR_PARSER) ErrorParser errorParser) {
        return new StorageLibraryPresenter(path, interactor, errorParser);
    }
}
