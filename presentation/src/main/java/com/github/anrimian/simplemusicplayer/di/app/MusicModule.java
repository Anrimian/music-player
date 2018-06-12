package com.github.anrimian.simplemusicplayer.di.app;


import android.content.Context;
import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.data.controllers.music.MusicPlayerControllerImpl;
import com.github.anrimian.simplemusicplayer.data.controllers.music.SystemMusicControllerImpl;
import com.github.anrimian.simplemusicplayer.data.database.AppDatabase;
import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.data.repositories.music.MusicProviderRepositoryImpl;
import com.github.anrimian.simplemusicplayer.data.repositories.play_queue.PlayQueueDataSource;
import com.github.anrimian.simplemusicplayer.data.repositories.play_queue.PlayQueueRepositoryImpl;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.IO_SCHEDULER;

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
                                                PlayQueueRepository playQueueRepository,
                                                MusicProviderRepository musicProviderRepository,
                                                Analytics analytics) {
        return new MusicPlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                playQueueRepository,
                musicProviderRepository,
                analytics);
    }

    @Provides
    @NonNull
    @Singleton
    PlayQueueRepository playQueueRepository(PlayQueueDataSource playQueueDataSource,
                                            UiStatePreferences uiStatePreferences,
                                            @Named(DB_SCHEDULER) Scheduler dbScheduler) {
        return new PlayQueueRepositoryImpl(playQueueDataSource, uiStatePreferences, dbScheduler);
    }

    @Provides
    @NonNull
    @Singleton
    PlayQueueDataSource playQueueDataSource(PlayQueueDao playQueueDao,
                                            StorageMusicDataSource storageMusicDataSource,
                                            SettingsPreferences settingsPreferences,
                                            @Named(DB_SCHEDULER) Scheduler dbScheduler) {
        return new PlayQueueDataSource(playQueueDao,
                storageMusicDataSource,
                settingsPreferences,
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
                                                       Context context) {
        return new MusicPlayerControllerImpl(uiStatePreferences, context);
    }

    @Provides
    @NonNull
    @Singleton
    MusicProviderRepository musicProviderRepository(StorageMusicDataSource storageMusicDataSource,
                                                    @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MusicProviderRepositoryImpl(storageMusicDataSource, scheduler);
    }
}
