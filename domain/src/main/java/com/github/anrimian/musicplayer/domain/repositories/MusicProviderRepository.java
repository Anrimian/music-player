package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface MusicProviderRepository {

    Observable<List<Composition>> getAllCompositionsObservable(@Nullable String searchText);

    Observable<Composition> getCompositionObservable(long id);

    Observable<List<Artist>> getArtistsObservable();

    Single<Folder> getCompositionsInPath(@Nullable String path, @Nullable String searchText);

    Single<List<Composition>> getAllCompositionsInPath(@Nullable String path);

    Single<List<Composition>> getAllCompositionsInFolders(Iterable<FileSource> fileSources);

    Single<List<String>> getAvailablePathsForPath(@Nullable String path);

    Completable writeErrorAboutComposition(ErrorType errorType, Composition composition);

    Completable deleteComposition(Composition composition);

    Completable deleteCompositions(List<Composition> compositions);

    Single<List<Composition>> changeFolderName(String folderPath, String newPath);

    Single<List<Composition>> moveFileTo(String folderPath,
                                         String newSourcePath,
                                         FileSource fileSource);
}
