package com.github.anrimian.musicplayer.data.repositories.library.edit;

import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

import android.os.Build;

import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.DuplicateFolderNamesException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.EditorTimeoutException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.FileExistsException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveFolderToItselfException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveInTheSameFolderException;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource;
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class EditorRepositoryImpl implements EditorRepository {

    private static final long CHANGE_COVER_TIMEOUT_MILLIS = 25000;

    private final CompositionSourceEditor sourceEditor;
    private final StorageFilesDataSource filesDataSource;
    private final CompositionsDaoWrapper compositionsDao;
    private final AlbumsDaoWrapper albumsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final GenresDaoWrapper genresDao;
    private final FoldersDaoWrapper foldersDao;
    private final StorageMusicProvider storageMusicProvider;
    private final StateRepository stateRepository;
    private final SettingsRepository settingsRepository;
    private final Scheduler scheduler;

    public EditorRepositoryImpl(CompositionSourceEditor sourceEditor,
                                StorageFilesDataSource filesDataSource,
                                CompositionsDaoWrapper compositionsDao,
                                AlbumsDaoWrapper albumsDao,
                                ArtistsDaoWrapper artistsDao,
                                GenresDaoWrapper genresDao,
                                FoldersDaoWrapper foldersDao,
                                StorageMusicProvider storageMusicProvider,
                                StateRepository stateRepository,
                                SettingsRepository settingsRepository,
                                Scheduler scheduler) {
        this.sourceEditor = sourceEditor;
        this.filesDataSource = filesDataSource;
        this.compositionsDao = compositionsDao;
        this.albumsDao = albumsDao;
        this.artistsDao = artistsDao;
        this.genresDao = genresDao;
        this.foldersDao = foldersDao;
        this.storageMusicProvider = storageMusicProvider;
        this.stateRepository = stateRepository;
        this.settingsRepository = settingsRepository;
        this.scheduler = scheduler;
    }

    /*
    rename genre - working
    add genre - working( no:( )
    change genre
    remove genre
    update\change album artist
    update genre name - not sure

    ******
    Seems, jaudiotagger names 'album-artist' and 'genre' differently than android media scanner

    We can add genre, but can't delete?

    Scan genres once?
     */

    @Override
    public Completable changeCompositionGenre(long compositionId,
                                              CompositionContentSource source,
                                              ShortGenre oldGenre,
                                              String newGenre) {
        return performSourceUpdate(source, sourceEditor.changeCompositionGenre(source, oldGenre.getName(), newGenre)
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> genresDao.changeCompositionGenre(compositionId, oldGenre.getId(), newGenre))
        );
    }

    @Override
    public Completable addCompositionGenre(long compositionId,
                                           CompositionContentSource source,
                                           String newGenre) {
        return performSourceUpdate(source, sourceEditor.addCompositionGenre(source, newGenre)
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> genresDao.addCompositionToGenre(compositionId, newGenre))
        );
    }

    @Override
    public Completable removeCompositionGenre(long compositionId,
                                              CompositionContentSource source,
                                              ShortGenre genre) {
        return performSourceUpdate(source, sourceEditor.removeCompositionGenre(source, genre.getName())
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> genresDao.removeCompositionFromGenre(compositionId, genre.getId()))
        );
    }

    @Override
    public Completable changeCompositionAuthor(long compositionId,
                                               CompositionContentSource source,
                                               String newAuthor) {
        return performSourceUpdate(source, sourceEditor.setCompositionAuthor(source, newAuthor)
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> compositionsDao.updateArtist(compositionId, newAuthor))
        );
    }

    @Override
    public Completable changeCompositionAlbumArtist(long compositionId,
                                                    CompositionContentSource source,
                                                    String newAuthor) {
        return performSourceUpdate(source, sourceEditor.setCompositionAlbumArtist(source, newAuthor)
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> compositionsDao.updateAlbumArtist(compositionId, newAuthor))
        );
    }

    @Override
    public Completable changeCompositionAlbum(long compositionId,
                                              CompositionContentSource source,
                                              String newAlbum) {
        return performSourceUpdate(source, sourceEditor.setCompositionAlbum(source, newAlbum)
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> compositionsDao.updateAlbum(compositionId, newAlbum))
        );
    }

    @Override
    public Completable changeCompositionTitle(long compositionId,
                                              CompositionContentSource source,
                                              String title) {
        return performSourceUpdate(source, sourceEditor.setCompositionTitle(source, title)
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> compositionsDao.updateTitle(compositionId, title))
        );
    }

    @Override
    public Completable changeCompositionLyrics(long compositionId,
                                               CompositionContentSource source,
                                               String text) {
        return performSourceUpdate(source, sourceEditor.setCompositionLyrics(source, text)
                .doOnComplete(() -> setCompositionInitialSourceToApp(compositionId))
                .doOnComplete(() -> compositionsDao.updateLyrics(compositionId, text))
        );
    }

    @Override
    public Completable changeCompositionFileName(FullComposition composition, String fileName) {
        return Completable.fromAction(() -> {
            Pair<String, String> newPathAndName = filesDataSource.renameCompositionFile(composition, fileName);
            compositionsDao.updateCompositionFileName(composition.getId(), newPathAndName.second);
            setCompositionInitialSourceToApp(composition.getId());
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable changeFolderName(long folderId, String newName) {
        return getFullFolderPath(folderId)
                .map(fullPath -> {
                    List<Composition> compositions = compositionsDao.getAllCompositionsInFolder(folderId, settingsRepository.isDisplayFileNameEnabled());

                    List<FilePathComposition> updatedCompositions = new LinkedList<>();
                    String newFileName = filesDataSource.renameCompositionsFolder(compositions,
                            fullPath,
                            newName,
                            updatedCompositions);
                    setCompositionsInitialSourceToApp(compositions);
                    return newFileName;
                })
                .doOnSuccess(name -> foldersDao.changeFolderName(folderId, name))
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    @Override
    public Completable moveFiles(Collection<FileSource> files,
                                 @Nullable Long fromFolderId,
                                 @Nullable Long toFolderId) {
        return verifyFolderMove(fromFolderId, toFolderId, files)
                .andThen(Single.zip(getFullFolderPath(fromFolderId),
                        getFullFolderPath(toFolderId),
                        foldersDao.extractAllCompositionsFromFiles(files, settingsRepository.isDisplayFileNameEnabled()),
                        (fromPath, toPath, compositions) -> {
                            filesDataSource.moveCompositionsToFolder(compositions, fromPath, toPath);
                            setCompositionsInitialSourceToApp(compositions);
                            return TRIGGER;
                        }))
                .ignoreElement()
                .doOnComplete(() -> foldersDao.updateFolderId(files, toFolderId))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable moveFilesToNewDirectory(Collection<FileSource> files,
                                               @Nullable Long fromFolderId,
                                               @Nullable Long targetParentFolderId,
                                               String directoryName) {
        return Completable.fromRunnable(() -> {
                    if (foldersDao.isFolderWithNameExists(targetParentFolderId, directoryName)) {
                        throw new FileExistsException();
                    }
                }).andThen(Single.zip(getFullFolderPath(fromFolderId),
                        getFullFolderPath(targetParentFolderId),
                        foldersDao.extractAllCompositionsFromFiles(files, settingsRepository.isDisplayFileNameEnabled()),
                        (fromPath, toParentPath, compositions) -> {
                            List<FilePathComposition> updatedCompositions = new LinkedList<>();
                            String name = filesDataSource.moveCompositionsToNewFolder(compositions,
                                    fromPath,
                                    toParentPath,
                                    directoryName,
                                    updatedCompositions);
                            setCompositionsInitialSourceToApp(compositions);
                            return foldersDao.createFolder(targetParentFolderId, name);
                        }))
                .doOnSuccess(folderId -> foldersDao.updateFolderId(files, folderId))
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateAlbumName(String name,
                                       List<Long> compositionIds,
                                       List<CompositionContentSource> sources,
                                       long albumId,
                                       BehaviorSubject<Long> editingSubject) {
        return performSourceUpdate(compositionIds, sources, sourceEditor.setCompositionsAlbum(sources, name, editingSubject)
                .doOnComplete(() -> albumsDao.updateAlbumName(name, albumId))
        );
    }

    @Override
    public Completable updateAlbumArtist(String artist,
                                         List<Long> compositionIds,
                                         List<CompositionContentSource> sources,
                                         long albumId,
                                         BehaviorSubject<Long> editingSubject) {
        return performSourceUpdate(compositionIds, sources, sourceEditor.setCompositionsAlbumArtist(sources, artist, editingSubject)
                .doOnComplete(() -> albumsDao.updateAlbumArtist(albumId, artist))
        );
    }

    @Override
    public Completable updateArtistName(String name,
                                        List<Long> compositionIds,
                                        List<CompositionContentSource> sources,
                                        long artistId,
                                        BehaviorSubject<Long> editingSubject) {
        return performSourceUpdate(compositionIds, sources, Single.fromCallable(() -> artistsDao.getAuthorName(artistId))
                .flatMapCompletable(oldName ->
                        sourceEditor.renameCompositionsAuthor(sources, oldName, name, editingSubject)
                ).doOnComplete(() -> artistsDao.updateArtistName(name, artistId))
        );
    }

    @Override
    public Completable updateGenreName(String name,
                                       List<Long> compositionIds,
                                       List<CompositionContentSource> sources,
                                       long genreId,
                                       BehaviorSubject<Long> editingSubject) {
        return performSourceUpdate(compositionIds, sources, Single.fromCallable(() -> genresDao.getGenreName(genreId))
                .flatMapCompletable(oldName ->
                        sourceEditor.setCompositionsGenre(sources, oldName, name, editingSubject)
                ).doOnComplete(() -> genresDao.updateGenreName(name, genreId))
        );
    }

    @Override
    public Completable changeCompositionAlbumArt(long compositionId,
                                                 CompositionContentSource source,
                                                 ImageSource imageSource) {
        return performSourceUpdate(source, sourceEditor.changeCompositionAlbumArt(source, imageSource)
                .doOnSuccess(newSize -> {
                    setCompositionInitialSourceToApp(compositionId);
                    compositionsDao.updateModifyTimeAndSize(compositionId, newSize, new Date());
                })
                .ignoreElement()
                .timeout(CHANGE_COVER_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS, Completable.error(new EditorTimeoutException()))
        );
    }

    @Override
    public Completable removeCompositionAlbumArt(long compositionId, CompositionContentSource source) {
        return performSourceUpdate(source, sourceEditor.removeCompositionAlbumArt(source)
                .doOnSuccess(newSize -> {
                    setCompositionInitialSourceToApp(compositionId);
                    compositionsDao.updateModifyTimeAndSize(compositionId, newSize, new Date());
                    runSystemRescan(source);
                })
                .ignoreElement()
                .timeout(CHANGE_COVER_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS, Completable.error(new EditorTimeoutException()))
        );
    }

    /**
     * Album-artist in android system and in common file has conflicts. This function
     * updates media library by real file source tags.
     */
    @Override
    public Completable updateTagsFromSource(CompositionContentSource source,
                                            FullComposition composition) {
        return sourceEditor.getFullTags(source)
                .flatMapCompletable(tags -> updateCompositionTags(composition, tags))
                .doOnComplete(() -> setCompositionInitialSourceToApp(composition.getId()))
                .subscribeOn(scheduler);
    }

    private Completable updateCompositionTags(FullComposition composition,
                                              CompositionSourceTags tags) {
        return Completable.fromAction(() ->
                compositionsDao.updateCompositionBySourceTags(composition, tags)
        );
    }

    private Single<String> getFullFolderPath(@Nullable Long folderId) {
        return Single.fromCallable(() -> {
            StringBuilder sbPath = new StringBuilder();
            String rootFolderPath = stateRepository.getRootFolderPath();
            if (rootFolderPath != null) {
                sbPath.append(rootFolderPath);
            }
            if (folderId != null) {
                if (sbPath.length() != 0) {
                    sbPath.append('/');
                }
                sbPath.append(foldersDao.getFullFolderPath(folderId));
            }
            return sbPath.toString();
        });
    }

    private Completable verifyFolderMove(@Nullable Long fromFolderId,
                                         @Nullable Long toFolderId,
                                         Collection<FileSource> files) {
        return Completable.fromAction(() -> {
            if (Objects.equals(fromFolderId, toFolderId)) {
                throw new MoveInTheSameFolderException("move in the same folder");
            }
            for (FileSource fileSource: files) {
                if (fileSource instanceof FolderFileSource) {
                    FolderFileSource folder = (FolderFileSource) fileSource;
                    long folderId = folder.getId();

                    List<Long> childFoldersId = foldersDao.getAllChildFoldersId(folderId);
                    if (Objects.equals(toFolderId, folderId) || childFoldersId.contains(toFolderId)) {
                        throw new MoveFolderToItselfException("moving and destination folders matches");
                    }
                    String name = foldersDao.getFolderName(folderId);
                    if (foldersDao.getChildFoldersNames(toFolderId).contains(name)) {
                        throw new DuplicateFolderNamesException();
                    }
                }
            }

        });
    }

    private Completable performSourceUpdate(CompositionContentSource source, Completable completable) {
        return completable
                .doOnSubscribe(o -> storageMusicProvider.setContentObserverEnabled(false))
                .doOnComplete(() -> {
                    storageMusicProvider.setContentObserverEnabled(true);
                    runSystemRescan(source);
                })
                .subscribeOn(scheduler);
    }


    private Completable performSourceUpdate(List<Long> compositionIds,
                                            List<CompositionContentSource> sources,
                                            Completable completable) {
        return completable
                .doOnSubscribe(o -> storageMusicProvider.setContentObserverEnabled(false))
                .doOnComplete(() -> {
                    setCompositionIdsInitialSourceToApp(compositionIds);
                    storageMusicProvider.setContentObserverEnabled(true);
                    runSystemRescan(sources);
                })
                .subscribeOn(scheduler);
    }

    private void runSystemRescan(List<CompositionContentSource> sources) {
        for (CompositionContentSource source: sources) {
            runSystemRescan(source);
        }
    }

    private void runSystemRescan(CompositionContentSource source) {
        if (source instanceof StorageCompositionSource) {
            StorageCompositionSource storageSource = (StorageCompositionSource) source;
            storageMusicProvider.scanMedia(storageSource.getUri());
        }
    }

    /**
     * Set initial source to app to display in-app delete dialog
     */
    private void setCompositionInitialSourceToApp(long id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            compositionsDao.updateCompositionInitialSource(id, InitialSource.APP, InitialSource.LOCAL);
        }
    }

    private void setCompositionsInitialSourceToApp(List<Composition> compositions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            compositionsDao.updateCompositionsInitialSource(compositions, InitialSource.APP, InitialSource.LOCAL);
        }
    }

    private void setCompositionIdsInitialSourceToApp(List<Long> compositionIds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            compositionsDao.updateCompositionIdsInitialSource(compositionIds, InitialSource.APP, InitialSource.LOCAL);
        }
    }
}
