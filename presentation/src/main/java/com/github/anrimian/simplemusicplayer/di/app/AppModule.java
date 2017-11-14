package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;
import android.support.annotation.NonNull;


import com.github.anrimian.simplemusicplayer.data.repositories.settings.Preferences;
import com.github.anrimian.simplemusicplayer.data.repositories.settings.SettingsRepositoryImpl;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsHelper;

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
    NotificationsHelper provideNotificationsController(Context context) {
        return new NotificationsHelper(context);
    }

    @Provides
    @Nonnull
    @Singleton
    Preferences providePrefenreces(Context context) {
        return new Preferences(context);
    }

    @Provides
    @Nonnull
    @Singleton
    SettingsRepository provideSettingsRepository(Preferences preferences) {
        return new SettingsRepositoryImpl(preferences);
    }
}
