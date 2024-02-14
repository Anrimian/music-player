package com.github.anrimian.musicplayer.domain;

public interface Constants {

    Object TRIGGER = new Object();
    int NO_POSITION = -1;
    long STORAGE_EVENTS_MIN_EMIT_WINDOW_MILLIS = 2000;

    int PLAY_QUEUE_MAX_ITEMS_COUNT = 30000;
    int PLAY_LIST_MAX_ITEMS_COUNT = 30000;

    char GENRE_DIVIDER_CHAR = ',';
    String GENRE_DIVIDER = GENRE_DIVIDER_CHAR + " ";
}
