package com.github.anrimian.simplemusicplayer.di.app;


import android.content.Context;
import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractorImpl;
import com.github.anrimian.simplemusicplayer.domain.business.player.state.PlayerStateInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.player.state.PlayerStateInteractorImpl;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerControllerOld;
import com.github.anrimian.simplemusicplayer.infrastructure.MusicPlayerControllerOldImpl;

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
    MusicPlayerInteractor provideMusicPlayerInteractor(MusicPlayerControllerOld musicPlayerController) {
        return new MusicPlayerInteractorImpl(musicPlayerController);
    }

    @Provides
    @NonNull
    @Singleton
    MusicPlayerControllerOld provideMusicPlayerController(Context context) {
        return new MusicPlayerControllerOldImpl(context);
    }

    @Provides
    @NonNull
    @Singleton
    PlayerStateInteractor providePlayerStateInteractor() {
        return new PlayerStateInteractorImpl();
    }
}
