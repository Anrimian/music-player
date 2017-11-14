package com.github.anrimian.simplemusicplayer.domain.repositories;

/**
 * Created on 14.11.2017.
 */

public interface SettingsRepository {

    void setRandomPlayingEnabled(boolean enabled);

    boolean isRandomPlayingEnabled();

    void setInfinitePlayingEnabled(boolean enabled);

    boolean isInfinitePlayingEnabled();
}
