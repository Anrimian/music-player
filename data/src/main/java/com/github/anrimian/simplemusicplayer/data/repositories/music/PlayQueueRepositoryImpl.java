package com.github.anrimian.simplemusicplayer.data.repositories.music;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;

import java.util.List;

/**
 * Created on 18.11.2017.
 */

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private PlayQueueDao playQueueDao;

    public PlayQueueRepositoryImpl(PlayQueueDao playQueueDao) {
        this.playQueueDao = playQueueDao;
    }

    @Override
    public void setCurrentPlayList(List<Composition> playList) {

    }

    @Override
    public List<Composition> getCurrentPlayList() {
        return null;
    }
}
