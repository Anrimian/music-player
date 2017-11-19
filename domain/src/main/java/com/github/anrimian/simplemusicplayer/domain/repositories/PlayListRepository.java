package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created on 18.11.2017.
 */

public interface PlayListRepository {

    Completable setCurrentPlayList(List<Composition> playList);

    Single<List<Composition>> getCurrentPlayList();
}
