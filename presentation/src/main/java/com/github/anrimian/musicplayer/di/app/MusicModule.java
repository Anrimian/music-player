package com.github.anrimian.musicplayer.di.app;


import android.content.Context;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.controllers.music.MusicPlayerControllerImpl;
import com.github.anrimian.musicplayer.data.controllers.music.SystemMusicControllerImpl;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.repositories.music.MusicProviderRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.music.folders.MusicFolderDataSource;
import com.github.anrimian.musicplayer.data.repositories.play_queue.PlayQueueRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 02.11.2017.
 */

@Module
class MusicModule {

    @Provides
    @NonNull
    @Singleton
    MusicPlayerInteractor musicPlayerInteractor(MusicPlayerController musicPlayerController,
                                                SettingsRepository settingsRepository,
                                                SystemMusicController systemMusicController,
                                                SystemServiceController systemServiceController,
                                                PlayQueueRepository playQueueRepository,
                                                MusicProviderRepository musicProviderRepository,
                                                Analytics analytics,
                                                PlayerErrorParser playerErrorParser) {
        return new MusicPlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                systemServiceController,
                playQueueRepository,
                musicProviderRepository,
                analytics,
                playerErrorParser);
    }

    @Provides
    @NonNull
    @Singleton
    PlayQueueRepository playQueueRepository(PlayQueueDaoWrapper playQueueDao,
                                            SettingsRepository settingsPreferences,
                                            UiStatePreferences uiStatePreferences,
                                            @Named(DB_SCHEDULER) Scheduler dbScheduler) {
        return new PlayQueueRepositoryImpl(playQueueDao,
                settingsPreferences,
                uiStatePreferences,
                dbScheduler);
    }

    @Provides
    @NonNull
    @Singleton
    SystemMusicController provideSystemMusicController(Context context) {
        return new SystemMusicControllerImpl(context);
    }

    @Provides
    @NonNull
    @Singleton
    MusicPlayerController provideMusicPlayerController(UiStatePreferences uiStatePreferences,
                                                       Context context,
                                                       @Named(UI_SCHEDULER) Scheduler scheduler) {
        return new MusicPlayerControllerImpl(uiStatePreferences, context, scheduler);
    }

    @Provides
    @NonNull
    @Singleton
    MusicProviderRepository musicProviderRepository(StorageMusicDataSource storageMusicDataSource,
                                                    CompositionsDaoWrapper compositionsDao,
                                                    MusicFolderDataSource musicFolderDataSource,
                                                    SettingsRepository settingsPreferences,
                                                    @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MusicProviderRepositoryImpl(storageMusicDataSource,
                compositionsDao,
                musicFolderDataSource,
                settingsPreferences,
                scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    MusicServiceInteractor musicServiceInteractor(SettingsRepository settingsRepository) {
        return new MusicServiceInteractor(settingsRepository);
    }
}
