package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.musicplayer.domain.utils.tree.visitors.CollectVisitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

/**
 * Created on 24.10.2017.
 */

public class LibraryFilesInteractor {

    private final MusicProviderRepository musicProviderRepository;
    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsRepository playListsRepository;
    private final SettingsRepository settingsRepository;

    public LibraryFilesInteractor(MusicProviderRepository musicProviderRepository,
                                  MusicPlayerInteractor musicPlayerInteractor,
                                  PlayListsRepository playListsRepository,
                                  SettingsRepository settingsRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsRepository = playListsRepository;
        this.settingsRepository = settingsRepository;
    }

    public Single<Folder> getCompositionsInPath(@Nullable String path, @Nullable String searchText) {
        return musicProviderRepository.getCompositionsInPath(path, searchText);
    }

    public Completable playAllMusicInPath(@Nullable String path) {
        return musicProviderRepository.getAllCompositionsInPath(path)
                .flatMapCompletable(musicPlayerInteractor::startPlaying);
    }

    public Completable playMusic(Composition composition) {
        return musicPlayerInteractor.startPlaying(asList(composition));
    }

    public Completable deleteCompositions(List<Composition> compositions) {
        return musicProviderRepository.deleteCompositions(compositions);
    }

    public Single<List<Composition>> deleteFolder(FolderFileSource folder) {
        return musicProviderRepository.getAllCompositionsInPath(folder.getFullPath())
                .doOnSuccess(o -> {});//delete recursive TODO finish
    }

    public Single<List<Composition>> addCompositionsToPlayList(String path, PlayList playList) {
        return musicProviderRepository.getAllCompositionsInPath(path)
                .flatMap(compositions -> playListsRepository.addCompositionsToPlayList(compositions, playList)
                                .toSingleDefault(compositions));
    }

    public Completable addCompositionsToPlayList(List<Composition> compositions, PlayList playList) {
        return playListsRepository.addCompositionsToPlayList(compositions, playList);
    }

    public void setFolderOrder(Order order) {
        settingsRepository.setFolderOrder(order);
    }

    public Order getFolderOrder() {
        return settingsRepository.getFolderOrder();
    }
}
