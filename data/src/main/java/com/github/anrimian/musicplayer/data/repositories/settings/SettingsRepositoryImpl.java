package com.github.anrimian.musicplayer.data.repositories.settings;

import com.github.anrimian.musicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

/**
 * Created on 14.11.2017.
 */

public class SettingsRepositoryImpl implements SettingsRepository {

    private SettingsPreferences settingsPreferences;

    public SettingsRepositoryImpl(SettingsPreferences settingsPreferences) {
        this.settingsPreferences = settingsPreferences;
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        settingsPreferences.setRandomPlayingEnabled(enabled);
    }

    @Override
    public boolean isRandomPlayingEnabled() {
        return settingsPreferences.isRandomPlayingEnabled();
    }

    @Override
    public void setInfinitePlayingEnabled(boolean enabled) {
        settingsPreferences.setInfinitePlayingEnabled(enabled);
    }

    @Override
    public boolean isInfinitePlayingEnabled() {
        return settingsPreferences.isInfinitePlayingEnabled();
    }

    @Override
    public void setFolderOrder(Order order) {
        settingsPreferences.setFolderOrder(order);
    }

    @Override
    public void setCompositionsOrder(Order order) {
        settingsPreferences.setCompositionsOrder(order);
    }

    @Override
    public Order getFolderOrder() {
        return settingsPreferences.getFolderOrder();
    }

    @Override
    public Order getCompositionsOrder() {
        return settingsPreferences.getCompositionsOrder();
    }
}
