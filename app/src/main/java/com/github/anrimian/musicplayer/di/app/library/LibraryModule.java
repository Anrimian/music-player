package com.github.anrimian.musicplayer.di.app.library;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.filesync.SyncInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderPresenter;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerPresenter;
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.LyricsPresenter;
import com.github.anrimian.musicplayer.ui.player_screen.queue.PlayQueuePresenter;
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

/**
 * Created on 29.10.2017.
 */
@Module
public class LibraryModule {

    @Provides
    @Nonnull
    PlayerPresenter playerPresenter(LibraryPlayerInteractor musicPlayerInteractor,
                                    PlayListsInteractor playListsInteractor,
                                    PlayerScreenInteractor playerScreenInteractor,
                                    ErrorParser errorParser,
                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new PlayerPresenter(musicPlayerInteractor,
                playListsInteractor,
                playerScreenInteractor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @Nonnull
    PlayQueuePresenter playQueuePresenter(LibraryPlayerInteractor musicPlayerInteractor,
                                          PlayListsInteractor playListsInteractor,
                                          PlayerScreenInteractor playerScreenInteractor,
                                          ErrorParser errorParser,
                                          @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new PlayQueuePresenter(musicPlayerInteractor,
                playListsInteractor,
                playerScreenInteractor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @Nonnull
    LyricsPresenter lyricsPresenter(LibraryPlayerInteractor libraryPlayerInteractor,
                                    EditorInteractor editorInteractor,
                                    ErrorParser errorParser,
                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new LyricsPresenter(
                libraryPlayerInteractor,
                editorInteractor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @NonNull
    @LibraryScope
    PlayerScreenInteractor playerScreenInteractor(SleepTimerInteractor sleepTimerInteractor,
                                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                                  SyncInteractor<?, ?, Long> syncInteractor,
                                                  UiStateRepository uiStateRepository,
                                                  SettingsRepository settingsRepository,
                                                  MediaScannerRepository mediaScannerRepository) {
        return new PlayerScreenInteractor(sleepTimerInteractor, libraryPlayerInteractor, syncInteractor, uiStateRepository, settingsRepository, mediaScannerRepository);
    }

    @Provides
    @Nonnull
    SelectOrderPresenter selectOrderPresenter(DisplaySettingsInteractor displaySettingsInteractor) {
        return new SelectOrderPresenter(displaySettingsInteractor);
    }

    @Provides
    @Nonnull
    ExcludedFoldersPresenter excludedFoldersPresenter(LibraryFoldersInteractor interactor,
                                                      @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                      ErrorParser errorParser) {
        return new ExcludedFoldersPresenter(interactor, uiScheduler, errorParser);
    }
}
