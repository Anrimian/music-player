package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

/**
 * Created on 18.11.2017.
 */

public interface PlayQueueRepository {

    void setCurrentPlayList(List<Composition> playList);

    List<Composition> getCurrentPlayList();
}
