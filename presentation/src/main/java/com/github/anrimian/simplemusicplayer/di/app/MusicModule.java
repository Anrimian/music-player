package com.github.anrimian.simplemusicplayer.di.app;


import android.content.Context;
import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractorImpl;
import com.github.anrimian.simplemusicplayer.domain.business.player.state.PlayerStateInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.player.state.PlayerStateInteractorImpl;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.infrastructure.MusicPlayerControllerImpl;

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
    MusicPlayerInteractor provideMusicPlayerInteractor(MusicPlayerController musicPlayerController) {
        return new MusicPlayerInteractorImpl(musicPlayerController);
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
    PlayerStateInteractor providePlayerStateInteractor() {
        return new PlayerStateInteractorImpl();
    }
}
