package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * Created on 24.10.2017.
 */

public interface LibraryRepository {

    Observable<List<Composition>> getAllCompositionsObservable(@Nullable String searchText);

    Observable<Composition> getCompositionObservable(long id);

    Observable<FullComposition> getFullCompositionObservable(long id);

    Observable<String> getLyricsObservable(long id);

    Observable<List<Artist>> getArtistsObservable(@Nullable String searchText);

    Observable<List<Album>> getAlbumsObservable(@Nullable String searchText);

    Observable<List<Genre>> getGenresObservable(@Nullable String searchText);

    Observable<List<ShortGenre>> getShortGenresInComposition(long compositionId);

    Observable<List<Composition>> getGenreItemsObservable(long genreId);

    Observable<List<Composition>> getAlbumItemsObservable(long albumId);

    Single<List<Long>> getCompositionIdsInAlbum(long albumId);

    Single<List<Long>> getAllCompositionsByArtist(long artistId);

    Single<List<Long>> getAllCompositionsByGenre(long genreId);

    Observable<Album> getAlbumObservable(long albumId);

    Observable<List<Composition>> getCompositionsByArtist(long artistId);

    Observable<Artist> getArtistObservable(long artistId);

    Observable<List<Album>> getAllAlbumsForArtist(long artistId);

    Single<String[]> getAuthorNames();

    Single<String[]> getAlbumNames();

    Single<String[]> getGenreNames();

    Observable<List<FileSource>> getFoldersInFolder(@Nullable Long folderId,
                                                    @Nullable String searchQuery);

    Observable<FolderFileSource> getFolderObservable(long folderId);

    Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId);

    Single<List<Composition>> getAllCompositionsInFolders(Iterable<FileSource> fileSources);

    Completable writeErrorAboutComposition(CorruptionType errorType, Composition composition);

    Completable deleteComposition(Composition composition);

    Completable deleteCompositions(List<Composition> compositions);

    Observable<Genre> getGenreObservable(long genreId);

    Single<IgnoredFolder> addFolderToIgnore(FolderFileSource folder);

    Completable addFolderToIgnore(IgnoredFolder folder);

    Observable<List<IgnoredFolder>> getIgnoredFoldersObservable();

    Completable deleteIgnoredFolder(IgnoredFolder folder);

    Single<List<Composition>> deleteFolder(FolderFileSource folder);

    Single<List<Composition>> deleteFolders(List<FileSource> folders);

    Single<List<Long>> getAllParentFolders(@Nullable Long folder);

    Single<List<Long>> getAllParentFoldersForComposition(long id);
}
