package com.github.anrimian.musicplayer.di.app.library;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderPresenter;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerPresenter;
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

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
    @NonNull
    PlayerScreenInteractor playerScreenInteractor(SleepTimerInteractor sleepTimerInteractor,
                                                  UiStateRepository uiStateRepository,
                                                  SettingsRepository settingsRepository) {
        return new PlayerScreenInteractor(sleepTimerInteractor, uiStateRepository, settingsRepository);
    }

    @Provides
    @Nonnull
    SelectOrderPresenter selectOrderPresenter() {
        return new SelectOrderPresenter();
    }

    @Provides
    @Nonnull
    ExcludedFoldersPresenter excludedFoldersPresenter(LibraryFoldersInteractor interactor,
                                                      @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                      ErrorParser errorParser) {
        return new ExcludedFoldersPresenter(interactor, uiScheduler, errorParser);
    }
}
