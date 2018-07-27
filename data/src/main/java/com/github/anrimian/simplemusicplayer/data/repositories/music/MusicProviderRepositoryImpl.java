package com.github.anrimian.simplemusicplayer.data.repositories.music;

import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.composition.AlphabeticalCompositionComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.composition.AlphabeticalDescCompositionComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.composition.CreateDateCompositionComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.composition.CreateDateDescCompositionComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.folder.AlphabeticalDescFileComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.folder.AlphabeticalFileComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.folder.CreateDateDescFileComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.folder.CreateDateFileComparator;
import com.github.anrimian.simplemusicplayer.data.repositories.music.folders.MusicFolderDataSource;
import com.github.anrimian.simplemusicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    public Observable<List<Composition>> getAllCompositionsObservable() {
        return storageMusicDataSource.getCompositionObservable()
                .map(this::toSortedList)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Folder> getCompositionsInPath(@Nullable String path) {
        return musicFolderDataSource.getCompositionsInPath(path)
                .doOnSuccess(folder -> folder.applyFileOrder(this::getFileComparator))
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

    private Comparator<FileSource> getFileComparator() {
        switch (settingsPreferences.getFolderOrder()) {
            case ALPHABETICAL: return new AlphabeticalFileComparator();
            case ALPHABETICAL_DESC: return new AlphabeticalDescFileComparator();
            case ADD_TIME: return new CreateDateFileComparator();
            case ADD_TIME_DESC: return new CreateDateDescFileComparator();
            default: return new AlphabeticalFileComparator();
        }
    }

    private Comparator<Composition> getCompositionComparator() {
        switch (settingsPreferences.getCompositionsOrder()) {
            case ALPHABETICAL: return new AlphabeticalCompositionComparator();
            case ALPHABETICAL_DESC: return new AlphabeticalDescCompositionComparator();
            case ADD_TIME: return new CreateDateCompositionComparator();
            case ADD_TIME_DESC: return new CreateDateDescCompositionComparator();
            default: return new AlphabeticalCompositionComparator();
        }
    }

    private Observable<Composition> getCompositionsObservable(@Nullable String path) {
        return musicFolderDataSource.getCompositionsInPath(path)
                .doOnSuccess(folder -> folder.applyFileOrder(this::getFileComparator))
                .flatMap(folder -> folder.getFilesObservable().firstOrError())
                .flatMapObservable(Observable::fromIterable)
                .flatMap(fileSource -> {
                    if (fileSource instanceof FolderFileSource) {
                        return getCompositionsObservable(((FolderFileSource) fileSource).getFullPath());
                    } else if (fileSource instanceof MusicFileSource) {
                        return Observable.just(((MusicFileSource) fileSource).getComposition());
                    }
                    throw new IllegalStateException("unexpected file source type: " + fileSource);
                });
    }

    private List<Composition> toSortedList(Map<Long, Composition> compositionMap) {
        List<Composition> list = new ArrayList<>(compositionMap.values());
        Collections.sort(list, getCompositionComparator());
        return list;
    }
}
