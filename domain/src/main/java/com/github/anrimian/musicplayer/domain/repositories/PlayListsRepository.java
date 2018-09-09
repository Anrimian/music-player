package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface PlayListsRepository {

    Observable<List<PlayList>> getPlayListsObservable();

    Observable<List<Composition>> getCompositionsObservable(long playlistId);

    Single<PlayList> createPlayList(String name);

    Completable addCompositionToPlayList(Composition composition, PlayList playList);
}
