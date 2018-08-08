package com.github.anrimian.simplemusicplayer.domain.business.playlists;

import com.github.anrimian.simplemusicplayer.domain.business.playlists.validators.PlayListNameValidator;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

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

    public Completable createPlayList(String name) {
        return nameValidator.validate(name)
                .flatMapCompletable(playListsRepository::createPlayList);
    }

    public Completable addCompositionToPlayList(Composition composition, PlayList playList) {
        return playListsRepository.addCompositionToPlayList(composition, playList);
    }
}
