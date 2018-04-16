package com.github.anrimian.simplemusicplayer.data.repositories.ui_state;

import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

/**
 * Created on 16.11.2017.
 */

public class UiStateRepositoryImpl implements UiStateRepository {

    private UiStatePreferences uiStatePreferences;

    public UiStateRepositoryImpl(UiStatePreferences uiStatePreferences) {
        this.uiStatePreferences = uiStatePreferences;
    }

    @Override
    public void setTrackPosition(long position) {
        uiStatePreferences.setTrackPosition(position);
    }

    @Override
    public long getTrackPosition() {
        return uiStatePreferences.getTrackPosition();
    }

    @Override
    public void setPlayListPosition(int position) {
        uiStatePreferences.setPlayQueuePosition(position);
    }

    @Override
    public int getPlayListPosition() {
        return uiStatePreferences.getCurrentCompositionPosition();
    }
}
