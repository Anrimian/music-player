package com.github.anrimian.musicplayer.domain.repositories;

import io.reactivex.rxjava3.core.Completable;

public interface MediaScannerRepository {
    void runStorageObserver();

    void rescanStorage();

    Completable runStorageScanner();
}
