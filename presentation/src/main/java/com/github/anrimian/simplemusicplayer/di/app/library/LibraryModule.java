package com.github.anrimian.simplemusicplayer.di.app.library;

import com.github.anrimian.simplemusicplayer.domain.business.library.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.PlayerPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 29.10.2017.
 */
@Module
public class LibraryModule {

    @Provides
    @Nonnull
    @LibraryScope
    StorageLibraryInteractor provideStorageLibraryInteractor(MusicProviderRepository musicProviderRepository,
                                                             MusicPlayerInteractor musicPlayerInteractor,
                                                             SettingsRepository settingsRepository) {
        return new StorageLibraryInteractor(musicProviderRepository,
                musicPlayerInteractor,
                settingsRepository);
    }

    @Provides
    @Nonnull
    PlayerPresenter provideLibraryPresenter(MusicPlayerInteractor musicPlayerInteractor,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new PlayerPresenter(musicPlayerInteractor, uiScheduler);
    }
}
