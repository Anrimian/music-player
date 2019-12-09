package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class EditorRepositoryImpl implements EditorRepository {

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor();

    private final StorageMusicDataSource storageMusicDataSource;
    private final AlbumsDaoWrapper albumsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final GenresDaoWrapper genresDao;
    private final Scheduler scheduler;

    public EditorRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                AlbumsDaoWrapper albumsDao,
                                ArtistsDaoWrapper artistsDao,
                                GenresDaoWrapper genresDao,
                                Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.albumsDao = albumsDao;
        this.artistsDao = artistsDao;
        this.genresDao = genresDao;
        this.scheduler = scheduler;
    }

    @Override
    public Completable changeCompositionGenre(FullComposition composition, String newGenre) {
        return sourceEditor.setCompositionGenre(composition.getFilePath(), newGenre)
                .andThen(storageMusicDataSource.updateCompositionGenre(composition, newGenre))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAuthor(FullComposition composition, String newAuthor) {
        return sourceEditor.setCompositionAuthor(composition.getFilePath(), newAuthor)
                .andThen(storageMusicDataSource.updateCompositionAuthor(composition, newAuthor))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAlbumArtist(FullComposition composition, String newAuthor) {
        return sourceEditor.setCompositionAlbumArtist(composition.getFilePath(), newAuthor)
                .andThen(storageMusicDataSource.updateCompositionAlbumArtist(composition, newAuthor))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAlbum(FullComposition composition, String newAlbum) {
        return sourceEditor.setCompositionAlbum(composition.getFilePath(), newAlbum)
                .andThen(storageMusicDataSource.updateCompositionAlbum(composition, newAlbum))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionTitle(FullComposition composition, String title) {
        return sourceEditor.setCompositionTitle(composition.getFilePath(), title)
                .andThen(storageMusicDataSource.updateCompositionTitle(composition, title))
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
        return Single.fromCallable(() -> albumsDao.getCompositionsInAlbum(albumId))
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(composition -> sourceEditor.setCompositionAlbum(composition.getFilePath(), name))
                .doOnComplete(() -> albumsDao.updateAlbumName(name, albumId))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateArtistName(String name, long artistId) {
        return Single.fromCallable(() -> artistsDao.getCompositionsByArtist(artistId))
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(composition -> sourceEditor.setCompositionAuthor(composition.getFilePath(), name))
                .doOnComplete(() -> artistsDao.updateArtistName(name, artistId))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateGenreName(String name, long genreId) {
        return Single.fromCallable(() -> genresDao.getCompositionsInGenre(genreId))
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(composition -> sourceEditor.setCompositionGenre(composition.getFilePath(), name))
                .doOnComplete(() -> genresDao.updateGenreName(name, genreId))
                .subscribeOn(scheduler);
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
