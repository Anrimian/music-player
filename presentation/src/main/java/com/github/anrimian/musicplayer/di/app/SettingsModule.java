package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.repositories.settings.SettingsRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.ui_state.UiStateRepositoryImpl;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 21.04.2018.
 */
@Module
public class SettingsModule {

    @Provides
    @Nonnull
    @Singleton
    SettingsRepository provideSettingsRepository(SettingsPreferences preferences) {
        return new SettingsRepositoryImpl(preferences);
    }

    @Provides
    @Nonnull
    @Singleton
    UiStateRepository provideUiStateRepository(UiStatePreferences preferences) {
        return new UiStateRepositoryImpl(preferences);
    }

    @Provides
    @Nonnull
    @Singleton
    SettingsPreferences settingsPreferences(Context context) {
        return new SettingsPreferences(context);
    }

    @Provides
    @Nonnull
    @Singleton
    UiStatePreferences uiStatePreferences(Context context) {
        return new UiStatePreferences(context);
    }
}
