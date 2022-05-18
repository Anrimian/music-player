package com.github.anrimian.musicplayer.di.app;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.repositories.settings.SettingsRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsPresenter;
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsPresenter;
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsPresenter;
import com.github.anrimian.musicplayer.ui.settings.player.impls.EnabledMediaPlayersPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

/**
 * Created on 21.04.2018.
 */
@Module
public class SettingsModule {

    @Provides
    @Nonnull
    @Singleton
    SettingsRepository provideSettingsRepository(Context context) {
        return new SettingsRepositoryImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    UiStateRepository provideUiStateRepository(Context context) {
        return new UiStateRepositoryImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    StateRepository uiStateRepository(Context context) {
        return new StateRepositoryImpl(context);
    }

    @Provides
    @Nonnull
    DisplaySettingsInteractor displaySettingsInteractor(SettingsRepository settingsRepository) {
        return new DisplaySettingsInteractor(settingsRepository);
    }

    @Provides
    @Nonnull
    DisplaySettingsPresenter displaySettingsPresenter(DisplaySettingsInteractor displaySettingsInteractor,
                                                      @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                      ErrorParser errorParser) {
        return new DisplaySettingsPresenter(displaySettingsInteractor, uiScheduler, errorParser);
    }

    @Provides
    @Nonnull
    PlayerSettingsInteractor playerSettingsInteractor(
            SettingsRepository settingsRepository,
            MusicPlayerController musicPlayerController
    ) {
        return new PlayerSettingsInteractor(settingsRepository, musicPlayerController);
    }

    @Provides
    @Nonnull
    PlayerSettingsPresenter playerSettingsPresenter(PlayerSettingsInteractor playerSettingsInteractor,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                    ErrorParser errorParser) {
        return new PlayerSettingsPresenter(playerSettingsInteractor, uiScheduler, errorParser);
    }

    @Provides
    @NonNull
    LibrarySettingsPresenter librarySettingsPresenter(LibrarySettingsInteractor librarySettingsInteractor,
                                                      @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                      ErrorParser errorParser) {
        return new LibrarySettingsPresenter(librarySettingsInteractor, uiScheduler, errorParser);
    }

    @Provides
    @NonNull
    LibrarySettingsInteractor librarySettingsInteractor(SettingsRepository settingsRepository,
                                                        MediaScannerRepository mediaScannerRepository) {
        return new LibrarySettingsInteractor(settingsRepository, mediaScannerRepository);
    }

    @Provides
    @Nonnull
    EnabledMediaPlayersPresenter enabledMediaPlayersPresenter(PlayerSettingsInteractor playerSettingsInteractor) {
        return new EnabledMediaPlayersPresenter(playerSettingsInteractor);
    }
}
