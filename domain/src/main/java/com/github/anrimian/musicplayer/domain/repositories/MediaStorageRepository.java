package com.github.anrimian.musicplayer.domain.repositories;

public interface MediaStorageRepository {
    void initialize();

    void rescanStorage();
}
