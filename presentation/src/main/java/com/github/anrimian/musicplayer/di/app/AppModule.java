package com.github.anrimian.musicplayer.di.app;

import android.content.Context;
import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.infrastructure.analytics.AnalyticsImpl;
import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.infrastructure.service.MusicServiceManager;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;

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
    MusicServiceManager serviceManager(Context context,
                                       MusicPlayerInteractor musicPlayerInteractor) {
        return new MusicServiceManager(context, musicPlayerInteractor);
    }

    @Provides
    @Nonnull
    @Singleton
    Analytics analytics() {
        return new AnalyticsImpl();
    }
}
