package com.github.anrimian.simplemusicplayer.di.app;


import android.content.Context;
import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.data.controllers.music.MusicPlayerControllerImpl;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.simplemusicplayer.infrastructure.MusicServiceControllerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 02.11.2017.
 */

@Module
class MusicModule {

    @Provides
    @NonNull
    @Singleton
    MusicPlayerInteractor provideMusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                                       MusicServiceController musicServiceController,
                                                       SettingsRepository settingsRepository,
                                                       UiStateRepository uiStateRepository) {
        return new MusicPlayerInteractor(musicPlayerController,
                musicServiceController,
                settingsRepository,
                uiStateRepository);
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
    MusicServiceController provideMusicServiceController(Context context) {
        return new MusicServiceControllerImpl(context);
    }
}
