package com.github.anrimian.simplemusicplayer.di.app;


import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractorImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 02.11.2017.
 */

@Module
public class MusicModule {

    @Provides
    @NonNull
    @Singleton
    MusicPlayerInteractor provideMusicPlayerInteractor() {
        return new MusicPlayerInteractorImpl();
    }
}
