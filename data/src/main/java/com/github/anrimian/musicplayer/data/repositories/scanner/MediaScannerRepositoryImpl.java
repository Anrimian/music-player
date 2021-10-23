package com.github.anrimian.musicplayer.data.repositories.scanner;

import android.database.sqlite.SQLiteDiskIOException;

import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.repositories.scanner.files.FileScanner;
import com.github.anrimian.musicplayer.data.storage.exceptions.ContentResolverQueryException;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenreItem;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MediaScannerRepositoryImpl implements MediaScannerRepository {

    private static final int RETRY_COUNT = 5;

    private final StorageMusicProvider musicProvider;
    private final StoragePlayListsProvider playListsProvider;
    private final StorageGenresProvider genresProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final GenresDaoWrapper genresDao;
    private final SettingsRepository settingsRepository;
    private final StorageCompositionAnalyzer compositionAnalyzer;
    private final StoragePlaylistAnalyzer playlistAnalyzer;
    private final FileScanner fileScanner;
    private final LoggerRepository loggerRepository;
    private final Analytics analytics;
    private final Scheduler scheduler;

    private final CompositeDisposable mediaStoreDisposable = new CompositeDisposable();
    private final LongSparseArray<Disposable> genreEntriesDisposable = new LongSparseArray<>();

    public MediaScannerRepositoryImpl(StorageMusicProvider musicProvider,
                                      StoragePlayListsProvider playListsProvider,
                                      StorageGenresProvider genresProvider,
                                      CompositionsDaoWrapper compositionsDao,
                                      GenresDaoWrapper genresDao,
                                      SettingsRepository settingsRepository,
                                      StorageCompositionAnalyzer compositionAnalyzer,
                                      StoragePlaylistAnalyzer playlistAnalyzer,
                                      FileScanner fileScanner,
                                      LoggerRepository loggerRepository,
                                      Analytics analytics,
                                      Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.playListsProvider = playListsProvider;
        this.genresProvider = genresProvider;
        this.compositionsDao = compositionsDao;
        this.genresDao = genresDao;
        this.settingsRepository = settingsRepository;
        this.compositionAnalyzer = compositionAnalyzer;
        this.playlistAnalyzer = playlistAnalyzer;
        this.fileScanner = fileScanner;
        this.loggerRepository = loggerRepository;
        this.analytics = analytics;
        this.scheduler = scheduler;
    }

    @Override
    public void runStorageObserver() {
        runRescanStorage()
                .doOnComplete(this::subscribeOnMediaStoreChanges)
                .subscribe();
    }

    @Override
    public synchronized void rescanStorage() {
        runRescanStorage().subscribe();
    }

    @Override
    public Completable runStorageScanner() {
        return runRescanStorage();
    }

    @Override
    public Completable runStorageAndFileScanner() {
        return Completable.fromAction(compositionsDao::cleanLastFileScanTime)
                .subscribeOn(scheduler)
                .andThen(runRescanStorage());
    }

    @Override
    public Observable<FileScannerState> getFileScannerStateObservable() {
        return fileScanner.getStateObservable();
    }

    private void subscribeOnMediaStoreChanges() {
        mediaStoreDisposable.add(getCompositionsObservable()
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .doOnNext(compositionAnalyzer::applyCompositionsData)
                .doOnNext(o -> fileScanner.scheduleFileScanner())
                .retry(RETRY_COUNT, this::isStandardError)
                .onErrorComplete(this::isStandardError)
                .subscribe(o -> {}));
        mediaStoreDisposable.add(playListsProvider.getPlayListsObservable()
                .subscribeOn(scheduler)
                .doOnNext(playlistAnalyzer::applyPlayListData)
                .retry(RETRY_COUNT, this::isStandardError)
                .onErrorComplete(this::isStandardError)
                .subscribe(o -> {}));

        //genre in files and genre in media store are ofter different, we need deep file scanner for them
        //<return genres after deep scan implementation>
//        mediaStoreDisposable.add(genresProvider.getGenresObservable()
//                .subscribeOn(scheduler)
//                .subscribe(this::onStorageGenresReceived));
//        subscribeOnGenresData();
    }

    //update on change settings not working
    private Observable<LongSparseArray<StorageFullComposition>> getCompositionsObservable() {
        return Observable.combineLatest(
                settingsRepository.getAudioFileMinDurationMillisObservable(),
                settingsRepository.getShowAllAudioFilesEnabledObservable(),
                Pair::new
        ).switchMap(settings -> musicProvider.getCompositionsObservable(
                settings.first,
                settings.second
        ));
    }

    private Completable runRescanStorage() {
        return Completable.fromAction(() -> {
            LongSparseArray<StorageFullComposition> compositions = musicProvider.getCompositions(
                    settingsRepository.getAudioFileMinDurationMillis(),
                    settingsRepository.isShowAllAudioFilesEnabled()
            );
            if (compositions == null) {
                return;
            }
            compositionAnalyzer.applyCompositionsData(compositions);
            LongSparseArray<StoragePlayList> playlists = playListsProvider.getPlayLists();
            if (playlists == null) {
                return;
            }
            playlistAnalyzer.applyPlayListData(playlists);
            fileScanner.scheduleFileScanner();

            //<return genres after deep scan implementation>
//            applyGenresData(genresProvider.getGenres());
//            List<IdPair> genresIds = genresDao.getGenresIds();
//            for (IdPair genreId: genresIds) {
//                long storageId = genreId.getStorageId();
//                long dbId = genreId.getDbId();
//                applyGenreItemsData(dbId, genresProvider.getGenreItems(storageId));
//            }
        }).onErrorResumeNext(this::processError)
                .doOnError(e -> loggerRepository.setWasCriticalFatalError(true))
                .subscribeOn(scheduler);
    }

    private Completable processError(Throwable throwable) {
        if (isStandardError(throwable)) {
            if (isStandardUnwantedError(throwable)) {
                analytics.processNonFatalError(throwable);
            }
            return Completable.complete();
        }
        return Completable.error(throwable);
    }

    private boolean isStandardError(Throwable throwable) {
        return throwable instanceof SQLiteDiskIOException
                || isStandardUnwantedError(throwable);
    }

    private boolean isStandardUnwantedError(Throwable throwable) {
        return throwable instanceof ContentResolverQueryException;
    }

    private void onStorageGenresReceived(Map<String, StorageGenre> newGenres) {
        applyGenresData(newGenres);
        subscribeOnGenresData();
    }

    private void subscribeOnGenresData() {
        List<IdPair> genresIds = genresDao.getGenresIds();
        for (IdPair genreId: genresIds) {
            long storageId = genreId.getStorageId();
            long dbId = genreId.getDbId();
            if (!genreEntriesDisposable.containsKey(dbId)) {
                Disposable disposable = genresProvider.getGenreItemsObservable(storageId)
                        .startWithItem(genresProvider.getGenreItems(storageId))
                        .subscribeOn(scheduler)
                        .subscribe(entries -> applyGenreItemsData(dbId, entries));
                genreEntriesDisposable.put(dbId, disposable);
            }
        }
    }

    private synchronized void applyGenreItemsData(long genreId,
                                                  LongSparseArray<StorageGenreItem> newGenreItems) {
        if (newGenreItems.isEmpty() || !genresDao.isGenreExists(genreId)) {
            genresDao.deleteGenre(genreId);
            genreEntriesDisposable.remove(genreId);
            return;
        }

        LongSparseArray<StorageGenreItem> currentItems = genresDao.selectAllAsStorageGenreItems(genreId);
        List<StorageGenreItem> addedItems = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentItems,
                newGenreItems,
                (o1, o2) -> false,
                item -> {},
                addedItems::add,
                item -> {});

        if (hasChanges) {
            genresDao.applyChanges(addedItems, genreId);
        }
    }

    private synchronized void applyGenresData(Map<String, StorageGenre> newGenres) {
        Set<String> currentGenres = genresDao.selectAllGenreNames();

        List<StorageGenre> addedGenres = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentGenres,
                newGenres,
                name -> name,
                StorageGenre::getName,
                (o1, o2) -> false,
                item -> {},
                addedGenres::add,
                (name, item) -> {});

        if (hasChanges) {
            genresDao.applyChanges(addedGenres);
        }
    }
}