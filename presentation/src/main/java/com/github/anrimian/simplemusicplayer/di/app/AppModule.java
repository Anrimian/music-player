package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.infrastructure.service.ServiceManager;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsDisplayer;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 11.02.2017.
 */

@Module
public class AppModule {

    private Context appContext;

    public AppModule(@NonNull Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @NonNull
    Context provideAppContext() {
        return appContext;
    }

    @Provides
    @Nonnull
    @Singleton
    NotificationsDisplayer provideNotificationsController(Context context) {
        return new NotificationsDisplayer(context);
    }

    @Provides
    @Nonnull
    @Singleton
    ServiceManager serviceManager(Context context, MusicPlayerInteractor musicPlayerInteractor) {
        return new ServiceManager(context, musicPlayerInteractor);
    }
}
