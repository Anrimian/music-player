package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.data.repositories.settings.SettingsRepositoryImpl;
import com.github.anrimian.simplemusicplayer.data.repositories.ui_state.UiStateRepositoryImpl;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

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
