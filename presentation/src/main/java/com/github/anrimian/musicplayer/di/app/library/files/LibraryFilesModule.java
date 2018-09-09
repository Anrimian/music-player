package com.github.anrimian.musicplayer.di.app.library.files;

import com.github.anrimian.musicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
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
public class LibraryFilesModule {

    @Nullable
    private String path;

    public LibraryFilesModule(@Nullable String path) {
        this.path = path;
    }

    @Provides
    @Nonnull
    LibraryFoldersPresenter libraryFoldersPresenter(LibraryFilesInteractor interactor,
                                                    PlayListsInteractor playListsInteractor,
                                                    @Named(STORAGE_ERROR_PARSER) ErrorParser errorParser,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new LibraryFoldersPresenter(path,
                interactor,
                playListsInteractor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @Nonnull
    LibraryFilesInteractor libraryFilesInteractor(MusicProviderRepository musicProviderRepository,
                                                  MusicPlayerInteractor musicPlayerInteractor,
                                                  SettingsRepository settingsRepository) {
        return new LibraryFilesInteractor(musicProviderRepository,
                musicPlayerInteractor,
                settingsRepository);
    }
}
