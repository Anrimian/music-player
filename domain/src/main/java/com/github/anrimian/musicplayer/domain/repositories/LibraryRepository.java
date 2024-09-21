package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderInfo;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.sync.FileKey;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kotlin.Pair;

/**
 * Created on 24.10.2017.
 */

public interface LibraryRepository {

    //compositions
    Observable<List<Composition>> getAllCompositionsObservable(@Nullable String searchText);

    Observable<Composition> getCompositionObservable(long id);

    Observable<FullComposition> getFullCompositionObservable(long id);

    Observable<String> getLyricsObservable(long id);

    Completable writeErrorAboutComposition(CorruptionType errorType, Composition composition);

    Single<DeletedComposition> deleteComposition(Composition composition);

    Single<List<DeletedComposition>> deleteCompositions(List<Composition> compositions);

    //folders
    Observable<List<FileSource>> getFoldersInFolder(@Nullable Long folderId,
                                                    @Nullable String searchQuery);

    Observable<FolderInfo> getFolderObservable(long folderId);

    Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId);

    Single<List<Composition>> getAllCompositionsInFolders(Iterable<FileSource> fileSources);

    Single<List<DeletedComposition>> deleteFolder(FolderFileSource folder);

    Single<List<DeletedComposition>> deleteFolders(List<FileSource> folders);

    Single<List<Long>> getAllParentFolders(@Nullable Long folder);

    Single<List<Long>> getAllParentFoldersForComposition(long id);

    Single<List<String>> getFolderNamesInPath(@Nullable String path);

    //artists
    Observable<List<Artist>> getArtistsObservable(@Nullable String searchText);

    Single<List<Long>> getAllCompositionIdsByArtists(long artistId);

    Single<List<Long>> getAllCompositionIdsByArtists(Iterable<Artist> artists);

    Single<List<Composition>> getAllCompositionsByArtists(Iterable<Artist> artists);

    Single<List<Composition>> getAllCompositionsByArtistIds(Iterable<Long> artists);

    Observable<List<Composition>> getCompositionsByArtist(long artistId);

    Observable<Artist> getArtistObservable(long artistId);

    Observable<List<Album>> getAllAlbumsForArtist(long artistId);

    Single<String[]> getAuthorNames();

    //albums
    Observable<List<Album>> getAlbumsObservable(@Nullable String searchText);

    Observable<List<AlbumComposition>> getAlbumItemsObservable(long albumId);

    Single<List<Long>> getCompositionIdsInAlbum(long albumId);

    Single<List<Long>> getCompositionIdsInAlbums(Iterable<Album> albums);

    Single<List<Composition>> getCompositionsInAlbums(Iterable<Album> albums);

    Single<List<Composition>> getCompositionsByAlbumIds(Iterable<Long> artists);

    Observable<Album> getAlbumObservable(long albumId);

    Single<String[]> getAlbumNames();

    //genres
    Observable<List<Genre>> getGenresObservable(@Nullable String searchText);

    Observable<List<Composition>> getGenreItemsObservable(long genreId);

    Single<List<Long>> getCompositionIdsInGenres(Iterable<Genre> genres);

    Single<List<Composition>> getCompositionsInGenres(Iterable<Genre> genres);

    Single<List<Composition>> getCompositionsInGenresIds(Iterable<Long> genresIds);

    Single<List<Long>> getAllCompositionsByGenre(long genreId);

    Single<String[]> getGenreNames(long forCompositionId);

    Observable<Genre> getGenreObservable(long genreId);

    //ignored folders
    Single<Pair<IgnoredFolder, List<FileKey>>> addFolderToIgnore(FolderFileSource folder);

    Single<List<FileKey>> addFolderToIgnore(IgnoredFolder folder);

    Observable<List<IgnoredFolder>> getIgnoredFoldersObservable();

    Single<List<FileKey>> deleteIgnoredFolder(IgnoredFolder folder);

    void deleteIgnoredFolder(String folderRelativePath);

}
