package com.github.anrimian.musicplayer.domain.repositories;

public interface MediaScannerRepository {
    void runStorageObserver();

    void rescanStorage();
}
