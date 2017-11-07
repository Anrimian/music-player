package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

/**
 * Created on 02.11.2017.
 */

public interface MusicPlayerInteractor {

    void startPlaying(List<Composition> compositions);

    void changePlayState();

    void skipToPrevious();

    void skipToNext();

}
