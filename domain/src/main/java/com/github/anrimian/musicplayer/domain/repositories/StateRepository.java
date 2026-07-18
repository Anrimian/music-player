package com.github.anrimian.musicplayer.domain.repositories;

public interface StateRepository {

    int getCurrentFileScannerVersion();

    void setLastFileScannerVersion(int version);

    int getLastFileScannerVersion();

    long getLastCompleteScanTime();

    void setLastCompleteScanTime(long scanTime);

    boolean isStoragePlaylistsImported();

    void setStoragePlaylistsImported(boolean isImported);

}
