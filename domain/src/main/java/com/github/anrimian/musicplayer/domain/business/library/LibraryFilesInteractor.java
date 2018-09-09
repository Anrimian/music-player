package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
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
    private final SettingsRepository settingsRepository;

    public LibraryFilesInteractor(MusicProviderRepository musicProviderRepository,
                                  MusicPlayerInteractor musicPlayerInteractor,
                                  SettingsRepository settingsRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.settingsRepository = settingsRepository;
    }

    public Single<Folder> getCompositionsInPath(@Nullable String path) {
        return musicProviderRepository.getCompositionsInPath(path);
    }

    public Completable playAllMusicInPath(@Nullable String path) {
        return musicProviderRepository.getAllCompositionsInPath(path)
                .flatMapCompletable(musicPlayerInteractor::startPlaying);
    }

    public Completable playMusic(Composition composition) {
        return musicPlayerInteractor.startPlaying(asList(composition));
    }

    public Completable deleteComposition(Composition composition) {
        return musicProviderRepository.deleteComposition(composition);
    }

    public void setFolderOrder(Order order) {
        settingsRepository.setFolderOrder(order);
    }

    public Order getFolderOrder() {
        return settingsRepository.getFolderOrder();
    }
}
