package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.music.edit.exceptions.AlbumAlreadyExistsException;
import com.github.anrimian.musicplayer.data.repositories.music.edit.exceptions.ArtistAlreadyExistsException;
import com.github.anrimian.musicplayer.data.repositories.music.edit.exceptions.FileExistsException;
import com.github.anrimian.musicplayer.data.repositories.music.edit.exceptions.GenreAlreadyExistsException;
import com.github.anrimian.musicplayer.data.repositories.music.edit.exceptions.MoveInTheSameFolderException;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class EditorRepositoryImpl implements EditorRepository {

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor();

    private final StorageMusicDataSource storageMusicDataSource;
    private final CompositionsDaoWrapper compositionsDao;
    private final AlbumsDaoWrapper albumsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final GenresDaoWrapper genresDao;
    private final StorageMusicProvider storageMusicProvider;
    private final StorageGenresProvider storageGenresProvider;
    private final StorageArtistsProvider storageArtistsProvider;
    private final StorageAlbumsProvider storageAlbumsProvider;
    private final Scheduler scheduler;

    public EditorRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                CompositionsDaoWrapper compositionsDao,
                                AlbumsDaoWrapper albumsDao,
                                ArtistsDaoWrapper artistsDao,
                                GenresDaoWrapper genresDao,
                                StorageMusicProvider storageMusicProvider,
                                StorageGenresProvider storageGenresProvider,
                                StorageArtistsProvider storageArtistsProvider,
                                StorageAlbumsProvider storageAlbumsProvider,
                                Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.compositionsDao = compositionsDao;
        this.albumsDao = albumsDao;
        this.artistsDao = artistsDao;
        this.genresDao = genresDao;
        this.storageMusicProvider = storageMusicProvider;
        this.storageGenresProvider = storageGenresProvider;
        this.storageArtistsProvider = storageArtistsProvider;
        this.storageAlbumsProvider = storageAlbumsProvider;
        this.scheduler = scheduler;
    }

    /*
    rename genre - working
    add genre - working( no:( )
    change genre
    remove genre
     */

    @Override
    public Completable changeCompositionGenre(FullComposition composition,
                                              ShortGenre oldGenre,
                                              String newGenre) {
        return sourceEditor.changeCompositionGenre(composition.getFilePath(), oldGenre.getName(), newGenre)
                .doOnComplete(() -> {
                    genresDao.changeCompositionGenre(composition.getId(), oldGenre.getId(), newGenre);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                //TODO update media storage
                .subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionGenre(FullComposition composition,
                                           String newGenre) {
        return sourceEditor.addCompositionGenre(composition.getFilePath(), newGenre)
                .doOnComplete(() -> {
                    genresDao.addCompositionToGenre(composition.getId(), newGenre);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                    //TODO not working
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable removeCompositionGenre(FullComposition composition, ShortGenre genre) {
        return sourceEditor.removeCompositionGenre(composition.getFilePath(), genre.getName())
                .doOnComplete(() -> {
                    genresDao.remoteCompositionFromGenre(composition.getId(), genre.getId());
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                //TODO update media storage
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAuthor(FullComposition composition, String newAuthor) {
        return sourceEditor.setCompositionAuthor(composition.getFilePath(), newAuthor)
                .doOnComplete(() -> {
                    compositionsDao.updateArtist(composition.getId(), newAuthor);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAlbumArtist(FullComposition composition, String newAuthor) {
        return sourceEditor.setCompositionAlbumArtist(composition.getFilePath(), newAuthor)
                .doOnComplete(() -> {
                    compositionsDao.updateAlbumArtist(composition.getId(), newAuthor);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAlbum(FullComposition composition, String newAlbum) {
        return sourceEditor.setCompositionAlbum(composition.getFilePath(), newAlbum)
                .doOnComplete(() -> {
                    compositionsDao.updateAlbum(composition.getId(), newAlbum);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionTitle(FullComposition composition, String title) {
        return sourceEditor.setCompositionTitle(composition.getFilePath(), title)
                .andThen(storageMusicDataSource.updateCompositionTitle(composition, title))
                .doOnComplete(() -> {
                    compositionsDao.updateTitle(composition.getId(), title);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionFileName(FullComposition composition, String fileName) {
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(composition.getFilePath(), fileName))
                .flatMap(newPath -> renameFile(composition.getFilePath(), newPath))
                .flatMapCompletable(newPath -> storageMusicDataSource.updateCompositionFilePath(composition, newPath))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionsFilePath(List<Composition> compositions) {
        return storageMusicDataSource.updateCompositionsFilePath(compositions)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String> changeFolderName(String filePath, String folderName) {
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(filePath, folderName))
                .flatMap(newPath -> renameFile(filePath, newPath))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String> moveFile(String filePath, String oldPath, String newPath) {
        if (Objects.equals(oldPath, newPath)) {
            return Single.error(new MoveInTheSameFolderException("move in the same folder"));
        }
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(filePath, oldPath, newPath))
                .flatMap(path -> renameFile(filePath, path))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable createFile(String path) {
        return Completable.fromAction(() -> {
            File file = new File(path);
            if (file.exists()) {
                throw new FileExistsException();
            }
            if (!file.mkdir()) {
                throw new Exception("file not created");
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable updateAlbumName(String name, long albumId) {
        return checkAlbumExists(name)
                .andThen(Single.fromCallable(() -> albumsDao.getCompositionsInAlbum(albumId)))
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(composition -> sourceEditor.setCompositionAlbum(composition.getFilePath(), name))
                .doOnComplete(() -> {
                    String oldName = albumsDao.getAlbumName(albumId);
                    String artist = albumsDao.getAlbumArtist(albumId);
                    albumsDao.updateAlbumName(name, albumId);
                    storageAlbumsProvider.updateAlbumName(oldName, artist, name);//not working
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateAlbumArtist(String newArtistName, long albumId) {
        return Single.fromCallable(() -> albumsDao.getCompositionsInAlbum(albumId))
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(composition -> sourceEditor.setCompositionAlbumArtist(composition.getFilePath(), newArtistName))
                .doOnComplete(() -> {
                    String albumName = albumsDao.getAlbumName(albumId);
                    String oldArtist = albumsDao.getAlbumArtist(albumId);
                    albumsDao.updateAlbumArtist(albumId, newArtistName);
                    storageAlbumsProvider.updateAlbumArtist(albumName, oldArtist, newArtistName);//not working
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateArtistName(String name, long artistId) {
        Set<Composition> compositionsToScan = new LinkedHashSet<>();
        return checkArtistExists(name)

                .andThen(Single.fromCallable(() -> artistsDao.getCompositionsByArtist(artistId)))
                .doOnSuccess(compositionsToScan::addAll)
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(composition -> sourceEditor.setCompositionAuthor(composition.getFilePath(), name))

                .andThen(Single.fromCallable(() -> albumsDao.getAllAlbumsForArtist(artistId)))
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(album -> Single.fromCallable(() -> albumsDao.getCompositionsInAlbum(album.getId()))
                                .doOnSuccess(compositionsToScan::addAll)
                                .flatMapObservable(Observable::fromIterable)
                                .flatMapCompletable(composition -> sourceEditor.setCompositionAlbumArtist(composition.getFilePath(), name))
                )
                .doOnComplete(() -> {
                    artistsDao.updateArtistName(name, artistId);

                    for (Composition composition: compositionsToScan) {
                        storageMusicProvider.scanMedia(composition.getFilePath());
                    }
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateGenreName(String name, long genreId) {
        return checkGenreExists(name)
                .andThen(Single.fromCallable(() -> genresDao.getGenreName(genreId)))
                .flatMapCompletable(oldName -> Single.fromCallable(() -> genresDao.getCompositionsInGenre(genreId))
                        .flatMapObservable(Observable::fromIterable)
                        .flatMapCompletable(composition -> sourceEditor.changeCompositionGenre(composition.getFilePath(), oldName, name))
                        .doOnComplete(() -> {
                            genresDao.updateGenreName(name, genreId);
                            storageGenresProvider.updateGenreName(oldName, name);
                        })
                )
                .subscribeOn(scheduler);
    }

    private Completable checkAlbumExists(String name) {
        return Completable.fromAction(() -> {
            if (albumsDao.isAlbumExists(name)) {
                throw new AlbumAlreadyExistsException();
            }
        });
    }

    private Completable checkArtistExists(String name) {
        return Completable.fromAction(() -> {
            if (artistsDao.isArtistExists(name)) {
                throw new ArtistAlreadyExistsException();
            }
        });
    }

    private Completable checkGenreExists(String name) {
        return Completable.fromAction(() -> {
            if (genresDao.isGenreExists(name)) {
                throw new GenreAlreadyExistsException();
            }
        });
    }

    private Single<String> renameFile(String oldPath, String newPath) {
        return Single.create(emitter -> {

            File oldFile = new File(oldPath);
            File newFile = new File(newPath);
            if (oldFile.renameTo(newFile)) {
                emitter.onSuccess(newPath);
            } else {
                emitter.onError(new Exception("file not renamed"));
            }
        });
    }



}
