package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created on 16.04.2018.
 */
public interface PlayQueueRepository {

    Completable setPlayQueue(List<Composition> compositions);

    Observable<Composition> getCurrentCompositionObservable();

    Observable<List<Composition>> getPlayQueueObservable();

    void setRandomPlayingEnabled(boolean enabled);

    void skipToNext();

    void skipToPrevious();

    void skipToPosition(int position);
}
