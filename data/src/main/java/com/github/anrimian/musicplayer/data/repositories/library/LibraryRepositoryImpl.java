package com.github.anrimian.musicplayer.data.repositories.library;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;

/**
 * Created on 24.10.2017.
 */

public class LibraryRepositoryImpl implements LibraryRepository {

    private final StorageFilesDataSource storageFilesDataSource;
    private final CompositionsDaoWrapper compositionsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final AlbumsDaoWrapper albumsDao;
    private final GenresDaoWrapper genresDao;
    private final FoldersDaoWrapper foldersDao;
    private final SettingsRepository settingsPreferences;
    private final Scheduler scheduler;

    public LibraryRepositoryImpl(StorageFilesDataSource storageFilesDataSource,
                                 CompositionsDaoWrapper compositionsDao,
                                 ArtistsDaoWrapper artistsDao,
                                 AlbumsDaoWrapper albumsDao,
                                 GenresDaoWrapper genresDao,
                                 FoldersDaoWrapper foldersDao,
                                 SettingsRepository settingsPreferences,
                                 Scheduler scheduler) {
        this.storageFilesDataSource = storageFilesDataSource;
        this.compositionsDao = compositionsDao;
        this.artistsDao = artistsDao;
        this.albumsDao = albumsDao;
        this.genresDao = genresDao;
        this.foldersDao = foldersDao;
        this.settingsPreferences = settingsPreferences;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<List<Composition>> getAllCompositionsObservable(@Nullable String searchText) {
        return settingsPreferences.getCompositionsOrderObservable()
                .switchMap(order -> settingsPreferences.getDisplayFileNameObservable()
                        .switchMap(useFileName -> compositionsDao.getAllObservable(
                                order,
                                useFileName,
                                searchText)
                        )
                );
    }

    @Override
    public Observable<Composition> getCompositionObservable(long id) {
        return settingsPreferences.getDisplayFileNameObservable()
                .switchMap(useFileName -> compositionsDao.getCompositionObservable(id, useFileName));
    }

    @Override
    public Observable<FullComposition> getFullCompositionObservable(long id) {
        return compositionsDao.getFullCompositionObservable(id);
    }

    @Override
    public Observable<String> getLyricsObservable(long id) {
        return compositionsDao.getLyricsObservable(id);
    }

    @Override
    public Completable writeErrorAboutComposition(CorruptionType corruptionType, Composition composition) {
        return Completable.fromAction(() -> compositionsDao.setCorruptionType(corruptionType, composition.getId()))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<DeletedComposition> deleteComposition(Composition composition) {
        return Single.fromCallable(() -> {
            long id = composition.getId();
            DeletedComposition deletedComposition = compositionsDao.selectDeletedComposition(id,
                    settingsPreferences.isDisplayFileNameEnabled());
            deletedComposition = storageFilesDataSource.deleteCompositionFile(deletedComposition);
            compositionsDao.delete(id);
            return deletedComposition;
        }).subscribeOn(scheduler);
    }

    @Override
    public Single<List<DeletedComposition>> deleteCompositions(List<Composition> compositions) {
        return Single.fromCallable(() -> {
            Long[] ids = ListUtils.mapToLongArray(compositions, Composition::getId);
            List<DeletedComposition> deletedCompositions = compositionsDao.selectDeletedComposition(
                    ids,
                    settingsPreferences.isDisplayFileNameEnabled()
            );
            deletedCompositions = storageFilesDataSource.deleteCompositionFiles(deletedCompositions, compositions);
            compositionsDao.deleteAll(ids);
            return deletedCompositions;
        }).subscribeOn(scheduler);
    }

    @Override
    public Observable<List<FileSource>> getFoldersInFolder(@Nullable Long folderId,
                                                           @Nullable String searchQuery) {
        return settingsPreferences.getFolderOrderObservable()
                .switchMap(order -> settingsPreferences.getDisplayFileNameObservable()
                        .switchMap(useFileName -> foldersDao.getFilesObservable(
                                folderId,
                                order,
                                useFileName,
                                searchQuery)
                        )
                );
    }

    @Override
    public Observable<FolderFileSource> getFolderObservable(long folderId) {
        return foldersDao.getFolderObservable(folderId);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId) {
        return Single.fromCallable(() -> selectAllCompositionsInFolder(folderId))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsInFolders(Iterable<FileSource> fileSources) {
        return foldersDao.extractAllCompositionsFromFiles(fileSources, settingsPreferences.getFolderOrder(), settingsPreferences.isDisplayFileNameEnabled())
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<DeletedComposition>> deleteFolder(FolderFileSource folder) {
        return Single.fromCallable(() -> compositionsDao.getAllCompositionsInFolder(
                        folder.getId(),
                        settingsPreferences.isDisplayFileNameEnabled()
                )).map(compositions -> {
                    Long[] ids = ListUtils.mapToLongArray(compositions, Composition::getId);
                    List<DeletedComposition> deletedCompositions = compositionsDao.selectDeletedComposition(
                            ids,
                            settingsPreferences.isDisplayFileNameEnabled()
                    );
                    deletedCompositions = storageFilesDataSource.deleteCompositionFiles(deletedCompositions, folder);
                    foldersDao.deleteFolder(folder.getId(), ids);
                    return deletedCompositions;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<DeletedComposition>> deleteFolders(List<FileSource> folders) {
        return foldersDao.extractAllCompositionsFromFiles(folders, settingsPreferences.isDisplayFileNameEnabled())
                .map(compositions -> {
                    Long[] ids = ListUtils.mapToLongArray(compositions, Composition::getId);
                    List<DeletedComposition> deletedCompositions = compositionsDao.selectDeletedComposition(
                            ids,
                            settingsPreferences.isDisplayFileNameEnabled()
                    );
                    deletedCompositions = storageFilesDataSource.deleteCompositionFiles(deletedCompositions, folders);
                    foldersDao.deleteFolders(extractFolderIds(folders), ids);
                    return deletedCompositions;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Long>> getAllParentFolders(@Nullable Long folderId) {
        return Single.fromCallable(() -> foldersDao.getAllParentFoldersId(folderId))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Long>> getAllParentFoldersForComposition(long id) {
        return Single.fromCallable(() -> {
            Long folderId = compositionsDao.getFolderId(id);
            return foldersDao.getAllParentFoldersId(folderId);
        }).subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Artist>> getArtistsObservable(@Nullable String searchText) {
        return settingsPreferences.getArtistsOrderObservable()
                .switchMap(order -> artistsDao.getAllObservable(order, searchText));
    }

    @Override
    public Single<List<Long>> getAllCompositionIdsByArtists(long artistId) {
        return artistsDao.getAllCompositionIdsByArtist(artistId)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Long>> getAllCompositionIdsByArtists(Iterable<Artist> artists) {
        return Observable.fromIterable(artists)
                .flatMapSingle(artist -> artistsDao.getAllCompositionIdsByArtist(artist.getId()))
                .<List<Long>>collect(ArrayList::new, List::addAll)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsByArtists(Iterable<Artist> artists) {
        return Observable.fromIterable(artists)
                .map(artist -> artistsDao.getAllCompositionsByArtist(
                        artist.getId(),
                        settingsPreferences.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsByArtistIds(Iterable<Long> artists) {
        return Observable.fromIterable(artists)
                .map(artisId -> artistsDao.getAllCompositionsByArtist(
                        artisId,
                        settingsPreferences.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Composition>> getCompositionsByArtist(long artistId) {
        return settingsPreferences.getDisplayFileNameObservable()
                .switchMap(useFileName -> artistsDao.getCompositionsByArtistObservable(artistId, useFileName));
    }

    @Override
    public Observable<Artist> getArtistObservable(long artistId) {
        return artistsDao.getArtistObservable(artistId);
    }

    @Override
    public Observable<List<Album>> getAllAlbumsForArtist(long artistId) {
        return albumsDao.getAllAlbumsForArtistObservable(artistId);
    }

    @Override
    public Single<String[]> getAuthorNames() {
        return Single.fromCallable(artistsDao::getAuthorNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Album>> getAlbumsObservable(@Nullable String searchText) {
        return settingsPreferences.getAlbumsOrderObservable()
                .switchMap(order -> albumsDao.getAllObservable(order, searchText));
    }

    @Override
    public Observable<List<AlbumComposition>> getAlbumItemsObservable(long albumId) {
        return settingsPreferences.getDisplayFileNameObservable()
                .switchMap(useFileName -> albumsDao.getCompositionsInAlbumObservable(albumId, useFileName));
    }

    @Override
    public Single<List<Long>> getCompositionIdsInAlbum(long albumId) {
        return albumsDao.getCompositionIdsInAlbum(albumId)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Long>> getCompositionIdsInAlbums(Iterable<Album> albums) {
        return Observable.fromIterable(albums)
                .flatMapSingle(album -> albumsDao.getCompositionIdsInAlbum(album.getId()))
                .<List<Long>>collect(ArrayList::new, List::addAll)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getCompositionsInAlbums(Iterable<Album> albums) {
        return Observable.fromIterable(albums)
                .map(album -> albumsDao.getCompositionsInAlbum(
                        album.getId(),
                        settingsPreferences.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getCompositionsByAlbumIds(Iterable<Long> albumIds) {
        return Observable.fromIterable(albumIds)
                .map(albumId -> albumsDao.getCompositionsInAlbum(
                        albumId,
                        settingsPreferences.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<Album> getAlbumObservable(long albumId) {
        return albumsDao.getAlbumObservable(albumId);
    }

    @Override
    public Single<String[]> getAlbumNames() {
        return Single.fromCallable(albumsDao::getAlbumNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Genre>> getGenresObservable(@Nullable String searchText) {
        return settingsPreferences.getGenresOrderObservable()
                .switchMap(order -> genresDao.getAllObservable(order, searchText));
    }

    @Override
    public Observable<List<ShortGenre>> getShortGenresInComposition(long compositionId) {
        return genresDao.getShortGenresInComposition(compositionId);
    }

    @Override
    public Observable<List<Composition>> getGenreItemsObservable(long genreId) {
        return settingsPreferences.getDisplayFileNameObservable()
                .switchMap(useFileName -> genresDao.getCompositionsInGenreObservable(genreId, useFileName));
    }

    @Override
    public Single<List<Long>> getAllCompositionsByGenre(long genreId) {
        return genresDao.getAllCompositionsByGenre(genreId)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String[]> getGenreNames() {
        return Single.fromCallable(genresDao::getGenreNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<Genre> getGenreObservable(long genreId) {
        return genresDao.getGenreObservable(genreId);
    }

    @Override
    public Single<IgnoredFolder> addFolderToIgnore(FolderFileSource folder) {
        return Single.fromCallable(() -> foldersDao.getFullFolderPath(folder.getId()))
                .map(foldersDao::insertIgnoredFolder)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable addFolderToIgnore(IgnoredFolder folder) {
        return Completable.fromAction(() ->
                foldersDao.insertIgnoredFolder(folder.getRelativePath(), folder.getAddDate())
        ).subscribeOn(scheduler);
    }

    @Override
    public Observable<List<IgnoredFolder>> getIgnoredFoldersObservable() {
        return foldersDao.getIgnoredFoldersObservable();
    }

    @Override
    public Completable deleteIgnoredFolder(IgnoredFolder folder) {
        return Completable.fromAction(() -> foldersDao.deleteIgnoredFolder(folder))
                .subscribeOn(scheduler);
    }

    private List<Long> extractFolderIds(List<FileSource> sources) {
        List<Long> result = new LinkedList<>();
        for (FileSource source : sources) {
            if (source instanceof FolderFileSource) {
                result.add(((FolderFileSource) source).getId());
            }
        }
        return result;
    }

    private List<Composition> selectAllCompositionsInFolder(Long folderId) {
        return foldersDao.getAllCompositionsInFolder(
                folderId,
                settingsPreferences.getFolderOrder(),
                settingsPreferences.isDisplayFileNameEnabled()
        );
    }
}
