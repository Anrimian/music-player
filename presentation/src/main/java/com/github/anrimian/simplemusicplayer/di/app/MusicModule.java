package com.github.anrimian.simplemusicplayer.di.app;


import android.content.Context;
import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.data.controllers.music.MusicPlayerControllerImpl;
import com.github.anrimian.simplemusicplayer.data.database.AppDatabase;
import com.github.anrimian.simplemusicplayer.data.repositories.playlist.PlayListRepositoryImpl;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.DB_SCHEDULER;

/**
 * Created on 02.11.2017.
 */

@Module
class MusicModule {

    @Provides
    @NonNull
    @Singleton
    MusicPlayerInteractor provideMusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                                       SettingsRepository settingsRepository,
                                                       UiStateRepository uiStateRepository,
                                                       PlayListRepository playListRepository) {
        return new MusicPlayerInteractor(musicPlayerController,
                settingsRepository,
                uiStateRepository,
                playListRepository);
    }

    @Provides
    @NonNull
    @Singleton
    MusicPlayerController provideMusicPlayerController(Context context) {
        return new MusicPlayerControllerImpl(context);
    }

    @Provides
    @NonNull
    @Singleton
    PlayListRepository providePlayListRepository(AppDatabase appDatabase,
                                                 @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new PlayListRepositoryImpl(appDatabase, scheduler);
    }
}
