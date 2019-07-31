package com.github.anrimian.musicplayer.di.app.library.files;

import com.github.anrimian.musicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.folders.root.FolderRootPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class LibraryFilesModule {

    @Provides
    @Nonnull
    FolderRootPresenter folderRootPresenter(LibraryFilesInteractor interactor,
                                            ErrorParser errorParser,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new FolderRootPresenter(interactor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @Nonnull
    @LibraryFilesScope
    LibraryFilesInteractor libraryFilesInteractor(MusicProviderRepository musicProviderRepository,
                                                  MusicPlayerInteractor musicPlayerInteractor,
                                                  PlayListsRepository playListsRepository,
                                                  SettingsRepository settingsRepository,
                                                  UiStateRepository uiStateRepository) {
        return new LibraryFilesInteractor(musicProviderRepository,
                musicPlayerInteractor,
                playListsRepository,
                settingsRepository,
                uiStateRepository);
    }
}
