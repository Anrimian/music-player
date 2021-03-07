package com.github.anrimian.musicplayer.domain.repositories;

public interface StateRepository {

    //root folder path is usable only before android 11

    String getRootFolderPath();

    void setRootFolderPath(String path);
}
