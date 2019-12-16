package com.github.anrimian.musicplayer.data.repositories.music;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.music.comparators.DescComparator;
import com.github.anrimian.musicplayer.data.repositories.music.comparators.composition.AlphabeticalCompositionComparator;
import com.github.anrimian.musicplayer.data.repositories.music.comparators.composition.CreateDateCompositionComparator;
import com.github.anrimian.musicplayer.data.repositories.music.comparators.folder.AlphabeticalFileComparator;
import com.github.anrimian.musicplayer.data.repositories.music.comparators.folder.CreateDateFileComparator;
import com.github.anrimian.musicplayer.data.repositories.music.comparators.folder.FolderComparator;
import com.github.anrimian.musicplayer.data.repositories.music.folders.MusicFolderDataSource;
import com.github.anrimian.musicplayer.data.repositories.music.search.FileSourceSearchFilter;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

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
    private final CompositionsDaoWrapper compositionsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final AlbumsDaoWrapper albumsDao;
    private final GenresDaoWrapper genresDao;
    private final MusicFolderDataSource musicFolderDataSource;
    private final SettingsRepository settingsPreferences;
    private final Scheduler scheduler;

    public MusicProviderRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                       CompositionsDaoWrapper compositionsDao,
                                       ArtistsDaoWrapper artistsDao,
                                       AlbumsDaoWrapper albumsDao,
                                       GenresDaoWrapper genresDao,
                                       MusicFolderDataSource musicFolderDataSource,
                                       SettingsRepository settingsPreferences,
                                       Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.compositionsDao = compositionsDao;
        this.artistsDao = artistsDao;
        this.albumsDao = albumsDao;
        this.genresDao = genresDao;
        this.musicFolderDataSource = musicFolderDataSource;
        this.settingsPreferences = settingsPreferences;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<List<Composition>> getAllCompositionsObservable(@Nullable String searchText) {
        return compositionsDao.getAllObservable(settingsPreferences.getCompositionsOrder(), searchText);
    }

    @Override
    public Observable<FullComposition> getCompositionObservable(long id) {
        return compositionsDao.getCompositionObservable(id);
    }

    @Override
    public Observable<List<Artist>> getArtistsObservable(@Nullable String searchText) {
        return settingsPreferences.getArtistsOrderObservable()
                .flatMap(order -> artistsDao.getAllObservable(order, null));
    }

    @Override
    public Observable<List<Album>> getAlbumsObservable() {
        return albumsDao.getAllObservable();
    }

    @Override
    public Observable<List<Genre>> getGenresObservable() {
        return genresDao.getAllObservable();
    }

    @Override
    public Observable<List<Composition>> getGenreItemsObservable(long genreId) {
        return genresDao.getCompositionsInGenreObservable(genreId);
    }

    @Override
    public Observable<List<Composition>> getAlbumItemsObservable(long albumId) {
        return albumsDao.getCompositionsInAlbumObservable(albumId);
    }

    @Override
    public Observable<Album> getAlbumObservable(long albumId) {
        return albumsDao.getAlbumObservable(albumId);
    }

    @Override
    public Observable<List<Composition>> getCompositionsByArtist(long artistId) {
        return artistsDao.getCompositionsByArtistObservable(artistId);
    }

    @Override
    public Observable<Artist> getArtistObservable(long artistId) {
        return artistsDao.getArtistObservable(artistId);
    }

    @Override
    public Observable<List<Album>> getAllAlbumsForArtist(long artistId) {
        return albumsDao.getAllAlbumsForArtist(artistId);
    }

    @Override
    public Single<String[]> getAuthorNames() {
        return Single.fromCallable(artistsDao::getAuthorNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String[]> getAlbumNames() {
        return Single.fromCallable(albumsDao::getAlbumNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String[]> getGenreNames() {
        return Single.fromCallable(genresDao::getGenreNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Folder> getCompositionsInPath(@Nullable String path,
                                                @Nullable String searchText) {
        return musicFolderDataSource.getCompositionsInPath(path)
                .doOnSuccess(folder -> folder.applyFileOrder(getSelectedFileComparatorObservable()))
                .doOnSuccess(folder -> folder.applySearchFilter(searchText, new FileSourceSearchFilter()))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsInPath(@Nullable String path) {
        return getCompositionsObservable(path)//FolderNodeNonExistException
                .toList()
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsInFolders(Iterable<FileSource> fileSources) {
        return extractAllCompositionsInFolders(fileSources)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<String>> getAvailablePathsForPath(@Nullable String path) {
        return musicFolderDataSource.getAvailablePathsForPath(path)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable writeErrorAboutComposition(CorruptionType corruptionType, Composition composition) {
        return Completable.fromAction(() -> compositionsDao.setCorruptionType(corruptionType, composition.getId()))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable deleteComposition(Composition composition) {
        return storageMusicDataSource.deleteComposition(composition)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable deleteCompositions(List<Composition> compositions) {
        return storageMusicDataSource.deleteCompositions(compositions)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> changeFolderName(String folderPath, String newPath) {
        return musicFolderDataSource.changeFolderName(folderPath, newPath);
    }

    @Override
    public Single<List<Composition>> moveFileTo(String folderPath,
                                                String newSourcePath,
                                                FileSource fileSource) {
        return musicFolderDataSource.moveFileTo(folderPath, newSourcePath, fileSource);
    }

    @Override
    public Observable<Genre> getGenreObservable(long genreId) {
        return genresDao.getGenreObservable(genreId);
    }

    private Comparator<FileSource> getFileComparator(Order order) {
        Comparator<FileSource> comparator;
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                comparator = new AlphabeticalFileComparator();
                break;
            }
            case ADD_TIME: {
                comparator = new CreateDateFileComparator();
                break;
            }
            default: return new AlphabeticalFileComparator();
        }
        if (order.isReversed()) {
            comparator = new DescComparator<>(comparator);
        }
        return new FolderComparator(comparator);
    }

    private Comparator<FileSource> getSelectedFileComparator() {
        return getFileComparator(settingsPreferences.getFolderOrder());
    }

    private Observable<Comparator<FileSource>> getSelectedFileComparatorObservable() {
        return settingsPreferences.getFolderOrderObservable()
                .map(this::getFileComparator);
    }

    private Comparator<Composition> getCompositionComparator() {
        Order order = settingsPreferences.getCompositionsOrder();
        Comparator<Composition> comparator;
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                comparator = new AlphabeticalCompositionComparator();
                break;
            }
            case ADD_TIME: {
                comparator = new CreateDateCompositionComparator();
                break;
            }
            default: return new AlphabeticalCompositionComparator();
        }
        if (order.isReversed()) {
            comparator = new DescComparator<>(comparator);
        }
        return comparator;
    }

    private Observable<Composition> getCompositionsObservable(@Nullable String path) {
        return musicFolderDataSource.getCompositionsInPath(path)
                .doOnSuccess(folder -> folder.applyFileOrder(this::getSelectedFileComparator))
                .flatMap(folder -> folder.getFilesObservable().firstOrError())
                .flatMapObservable(Observable::fromIterable)
                .flatMap(fileSource -> {
                    if (fileSource instanceof FolderFileSource) {
                        return getCompositionsObservable(((FolderFileSource) fileSource).getPath());
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

    private Observable<Composition> findComposition(Map<Long, Composition> compositions, long id) {
        return Observable.create(emitter -> {
            Composition composition = compositions.get(id);
            if (composition == null) {
                emitter.onComplete();
            } else {
                emitter.onNext(composition);
            }
        });
    }

    private Single<List<Composition>> extractAllCompositionsInFolders(Iterable<FileSource> fileSources) {
        return Observable.fromIterable(fileSources)
                .flatMap(this::fileSourceToComposition)
                .collect(ArrayList::new, List::add);
    }

    private Observable<Composition> fileSourceToComposition(FileSource fileSource) {
        if (fileSource instanceof MusicFileSource) {
            return Observable.just(((MusicFileSource) fileSource).getComposition());
        }
        if (fileSource instanceof FolderFileSource) {
            return getCompositionsObservable(((FolderFileSource) fileSource).getPath());
        }
        throw new IllegalStateException("unexpected file source: " + fileSource);
    }
}
