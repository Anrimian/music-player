package com.github.anrimian.simplemusicplayer.data.repositories.settings;

import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

/**
 * Created on 14.11.2017.
 */

public class SettingsRepositoryImpl implements SettingsRepository {

    private Preferences preferences;

    public SettingsRepositoryImpl(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        preferences.setRandomPlayingEnabled(enabled);
    }

    @Override
    public boolean isRandomPlayingEnabled() {
        return preferences.isRandomPlayingEnabled();
    }

    @Override
    public void setInfinitePlayingEnabled(boolean enabled) {
        preferences.setInfinitePlayingEnabled(enabled);
    }

    @Override
    public boolean isInfinitePlayingEnabled() {
        return preferences.isInfinitePlayingEnabled();
    }
}
