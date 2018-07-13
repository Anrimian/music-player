package com.github.anrimian.simplemusicplayer.data.repositories.music;

import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.repositories.music.folders.MusicFolderDataSource;
import com.github.anrimian.simplemusicplayer.data.repositories.music.sort.folder.AlphabeticalDescFolderSorter;
import com.github.anrimian.simplemusicplayer.data.repositories.music.sort.folder.AlphabeticalFolderSorter;
import com.github.anrimian.simplemusicplayer.data.repositories.music.sort.Sorter;
import com.github.anrimian.simplemusicplayer.data.repositories.music.sort.folder.CreateDateDescFolderSorter;
import com.github.anrimian.simplemusicplayer.data.repositories.music.sort.folder.CreateDateFolderSorter;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class MusicProviderRepositoryImpl implements MusicProviderRepository {

    private final StorageMusicDataSource storageMusicDataSource;
    private final MusicFolderDataSource musicFolderDataSource;
    private final SettingsPreferences settingsPreferences;
    private final Scheduler scheduler;

    public MusicProviderRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                       MusicFolderDataSource musicFolderDataSource,
                                       SettingsPreferences settingsPreferences,
                                       Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.musicFolderDataSource = musicFolderDataSource;
        this.settingsPreferences = settingsPreferences;
        this.scheduler = scheduler;
    }

    @Override
    public Single<List<Composition>> getAllCompositions() {
        return storageMusicDataSource.getCompositions()
                .map(compositions -> (List<Composition>) new ArrayList(compositions.values()))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Folder> getCompositionsInPath(@Nullable String path) {
        return musicFolderDataSource.getCompositionsInPath(path)
                .doOnSuccess(getFolderSorter()::applyOrder)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsInPath(@Nullable String path) {
        return getCompositionsObservable(path)
                .toList()
                .subscribeOn(scheduler);
    }

    @Override
    public Completable writeErrorAboutComposition(ErrorType errorType, Composition composition) {
        return Completable.complete()//TODO write error about composition
                .subscribeOn(scheduler);
    }

    @Override
    public Completable deleteComposition(Composition composition) {
        return storageMusicDataSource.deleteComposition(composition);
    }

    private Sorter<Folder> getFolderSorter() {
        switch (settingsPreferences.getFolderOrder()) {
            case ALPHABETICAL: return new AlphabeticalFolderSorter();
            case ALPHABETICAL_DESC: return new AlphabeticalDescFolderSorter();
            case CREATE_TIME: return new CreateDateFolderSorter();
            case CREATE_TIME_DESC: return new CreateDateDescFolderSorter();
            default: return new AlphabeticalFolderSorter();
        }
    }

    private Observable<Composition> getCompositionsObservable(@Nullable String path) {
//        return musicFolderDataSource.getCompositionsInPath(path)
//                .doOnSuccess(getFolderSorter()::applyOrder)
//                .map(Folder::getFiles)
//                .flatMapObservable(Observable::fromIterable)
//                .flatMap(fileSource -> {
//                    if (fileSource instanceof FolderFileSource) {
//                        return getCompositionsObservable(((FolderFileSource) fileSource).getFullPath());
//                    } else if (fileSource instanceof MusicFileSource) {
//                        return Observable.just(((MusicFileSource) fileSource).getComposition());
//                    }
//                    throw new IllegalStateException("unexpected file source type: " + fileSource);
//                });
        return null;
    }
}
