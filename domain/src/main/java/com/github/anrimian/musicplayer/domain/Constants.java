package com.github.anrimian.musicplayer.domain;

public interface Constants {

    Object TRIGGER = new Object();
    int NO_POSITION = -1;
    long STORAGE_EVENTS_MIN_EMIT_WINDOW_MILLIS = 2000;

    String PLAYLIST_EXTENSION = ".m3u";
    byte MAX_FILE_NAME_LENGTH = 90;
    String PLAYLIST_NOT_ALLOWED_CHARACTERS = "[\\\\/:*?\"<>|]";

    int PLAYLIST_NAME_MAX_LENGTH = MAX_FILE_NAME_LENGTH - PLAYLIST_EXTENSION.length();
}
