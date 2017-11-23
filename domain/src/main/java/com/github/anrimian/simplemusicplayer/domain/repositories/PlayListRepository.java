package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.playlist.CurrentPlayListInfo;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created on 18.11.2017.
 */

public interface PlayListRepository {

    Completable setCurrentPlayList(CurrentPlayListInfo currentPlayListInfo);

    Single<CurrentPlayListInfo> getCurrentPlayList();
}
