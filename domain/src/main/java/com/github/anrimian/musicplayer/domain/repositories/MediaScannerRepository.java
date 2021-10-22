package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface MediaScannerRepository {
    void runStorageObserver();

    void rescanStorage();

    Completable runStorageScanner();

    Completable runStorageAndFileScanner();

    Observable<FileScannerState> getFileScannerStateObservable();
}
