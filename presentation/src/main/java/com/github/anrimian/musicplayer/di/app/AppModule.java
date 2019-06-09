package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.infrastructure.analytics.AnalyticsImpl;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder;
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater;

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
    NotificationsDisplayer provideNotificationsController(Context context,
                                                          AppNotificationBuilder notificationBuilder) {
        return new NotificationsDisplayer(context, notificationBuilder);
    }

    @Provides
    @Nonnull
    @Singleton
    AppNotificationBuilder appNotificationBuilder() {
        return new AppNotificationBuilder();
    }

    @Provides
    @Nonnull
    @Singleton
    SystemServiceController systemServiceController(Context context) {
        return new SystemServiceControllerImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    Analytics analytics() {
        return new AnalyticsImpl();
    }

    @Provides
    @NonNull
    @Singleton
    WidgetUpdater widgetUpdater(Context context, MusicPlayerInteractor playerInteractor) {
        return new WidgetUpdater(context, playerInteractor);
    }
}
