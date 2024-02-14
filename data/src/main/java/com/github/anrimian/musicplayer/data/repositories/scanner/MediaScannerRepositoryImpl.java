package com.github.anrimian.musicplayer.data.repositories.scanner;

import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDiskIOException;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.scanner.files.FileScanner;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.StoragePlaylistsAnalyzer;
import com.github.anrimian.musicplayer.data.storage.exceptions.ContentResolverQueryException;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;

import java.util.Collections;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MediaScannerRepositoryImpl implements MediaScannerRepository {

    private static final int RETRY_COUNT = 5;

    private final StorageMusicProvider musicProvider;
    private final StoragePlayListsProvider playListsProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final StateRepository stateRepository;
    private final SettingsRepository settingsRepository;
    private final StorageCompositionAnalyzer compositionAnalyzer;
    private final StoragePlaylistsAnalyzer playlistAnalyzer;
    private final FileScanner fileScanner;
    private final LoggerRepository loggerRepository;
    private final Analytics analytics;
    private final Scheduler scheduler;

    private final CompositeDisposable mediaStoreDisposable = new CompositeDisposable();

    public MediaScannerRepositoryImpl(StorageMusicProvider musicProvider,
                                      StoragePlayListsProvider playListsProvider,
                                      CompositionsDaoWrapper compositionsDao,
                                      StateRepository stateRepository,
                                      SettingsRepository settingsRepository,
                                      StorageCompositionAnalyzer compositionAnalyzer,
                                      StoragePlaylistsAnalyzer playlistAnalyzer,
                                      FileScanner fileScanner,
                                      LoggerRepository loggerRepository,
                                      Analytics analytics,
                                      Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.playListsProvider = playListsProvider;
        this.compositionsDao = compositionsDao;
        this.stateRepository = stateRepository;
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
    public void rescanStorage() {
        try {
            LongSparseArray<StorageFullComposition> compositions = musicProvider.getCompositions(
                    settingsRepository.getAudioFileMinDurationMillis(),
                    settingsRepository.isShowAllAudioFilesEnabled()
            );
            if (compositions == null) {
                return;
            }
            compositionAnalyzer.applyCompositionsData(compositions);

            Map<String, StoragePlayList> playlists;
            if (stateRepository.isStoragePlaylistsImported()) {
                playlists = Collections.emptyMap();
            } else {
                playlists = playListsProvider.getPlayLists();
                if (playlists == null) {
                    return;
                }
                stateRepository.setStoragePlaylistsImported(true);
            }
            //it should always be called to trigger file cache analyze on app startup
            playlistAnalyzer.applyPlayListsData(playlists);

            fileScanner.scheduleFileScanner();
        } catch (Exception e) {
            if (isStandardError(e)) {
                if (isStandardUnwantedError(e)) {
                    analytics.processNonFatalError(e);
                }
                return;
            }
            loggerRepository.setWasCriticalFatalError(true);
            throw e;
        }
    }

    @Override
    public synchronized void rescanStorageAsync() {
        runRescanStorage().subscribe();
    }

    @NonNull
    @Override
    public Completable rescanStoragePlaylists() {
        return Completable.fromAction(this::readStoragePlaylists)
                .subscribeOn(scheduler);
    }

    @NonNull
    @Override
    public Completable runStorageScanner() {
        return runRescanStorage();
    }

    @NonNull
    @Override
    public Completable runStorageAndFileScanner() {
        return Completable.fromAction(compositionsDao::cleanLastFileScanTime)
                .subscribeOn(scheduler)
                .andThen(runRescanStorage());
    }

    @NonNull
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
        return Completable.fromAction(this::rescanStorage)
                .subscribeOn(scheduler);
    }

    private void readStoragePlaylists() {
        Map<String, StoragePlayList> playlists = playListsProvider.getPlayLists();
        if (playlists == null) {
            return;
        }
        playlistAnalyzer.applyPlayListsData(playlists);
        stateRepository.setStoragePlaylistsImported(true);
    }

    private boolean isStandardError(Throwable throwable) {
        return throwable instanceof SQLiteDiskIOException
                || throwable instanceof SQLiteCantOpenDatabaseException
                || isStandardUnwantedError(throwable);
    }

    private boolean isStandardUnwantedError(Throwable throwable) {
        return throwable instanceof ContentResolverQueryException;
    }
}