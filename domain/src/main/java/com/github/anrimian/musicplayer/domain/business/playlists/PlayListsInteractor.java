package com.github.anrimian.musicplayer.domain.business.playlists;

import com.github.anrimian.musicplayer.domain.business.playlists.validators.PlayListNameValidator;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class PlayListsInteractor {

    private final PlayListNameValidator nameValidator = new PlayListNameValidator();

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

    public Single<PlayList> createPlayList(String name) {
        return nameValidator.validate(name)
                .flatMap(playListsRepository::createPlayList);
    }

    public Completable addCompositionToPlayList(Composition composition, PlayList playList) {
        return playListsRepository.addCompositionToPlayList(composition, playList);
    }

    public Completable addCompositionsToPlayList(List<Composition> compositions, PlayList playList) {
        return Observable.fromIterable(compositions)
                .flatMapCompletable(composition -> addCompositionToPlayList(composition, playList));
    }
}
