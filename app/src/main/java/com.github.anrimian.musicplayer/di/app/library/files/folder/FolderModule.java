package com.github.anrimian.musicplayer.di.app.library.files.folder;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

/**
 * Created on 31.10.2017.
 */

@Module
public class FolderModule {

    @Nullable
    private final Long folderId;

    public FolderModule(@Nullable Long folderId) {
        this.folderId = folderId;
    }

    @Provides
    @Nonnull
    LibraryFoldersPresenter libraryFoldersPresenter(LibraryFoldersScreenInteractor interactor,
                                                    LibraryPlayerInteractor playerInteractor,
                                                    DisplaySettingsInteractor displaySettingsInteractor,
                                                    ErrorParser errorParser,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new LibraryFoldersPresenter(folderId,
                interactor,
                playerInteractor,
                displaySettingsInteractor,
                errorParser,
                uiScheduler);
    }
}
