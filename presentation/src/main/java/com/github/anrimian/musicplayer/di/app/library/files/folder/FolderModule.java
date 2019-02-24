package com.github.anrimian.musicplayer.di.app.library.files.folder;

import com.github.anrimian.musicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.ErrorModule.STORAGE_ERROR_PARSER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 31.10.2017.
 */

@Module
public class FolderModule {

    @Nullable
    private String path;

    public FolderModule(@Nullable String path) {
        this.path = path;
    }

    @Provides
    @Nonnull
    LibraryFoldersPresenter libraryFoldersPresenter(LibraryFilesInteractor interactor,
                                                    @Named(STORAGE_ERROR_PARSER) ErrorParser errorParser,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new LibraryFoldersPresenter(path,
                interactor,
                errorParser,
                uiScheduler);
    }
}
