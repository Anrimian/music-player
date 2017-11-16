package com.github.anrimian.simplemusicplayer.domain.repositories;

/**
 * Created on 16.11.2017.
 */

public interface UiStateRepository {

    void setTrackPosition(long position);

    long getTrackPosition();

    void setPlayListPosition(int position);

    int getPlayListPosition();
}
