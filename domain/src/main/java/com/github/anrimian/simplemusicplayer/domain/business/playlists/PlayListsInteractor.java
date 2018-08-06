package com.github.anrimian.simplemusicplayer.domain.business.playlists;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListsRepository;

import java.util.List;

import io.reactivex.Observable;

public class PlayListsInteractor {

    private final PlayListsRepository playListsRepository;

    public PlayListsInteractor(PlayListsRepository playListsRepository) {
        this.playListsRepository = playListsRepository;
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return playListsRepository.getPlayListsObservable();
    }

    public Observable<List<Composition>> getCompositionsObservable(long playlistId) {
        return playListsRepository.getCompositionsObservable(playlistId);
    }
}
