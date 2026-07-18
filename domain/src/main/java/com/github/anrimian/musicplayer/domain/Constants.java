package com.github.anrimian.musicplayer.domain;

import java.util.Set;

import kotlin.collections.SetsKt;

public interface Constants {

    Object TRIGGER = new Object();
    int NO_POSITION = -1;
    long STORAGE_EVENTS_MIN_EMIT_WINDOW_MILLIS = 2000;

    int PLAY_QUEUE_MAX_ITEMS_COUNT = 30000;
    int PLAY_LIST_MAX_ITEMS_COUNT = 30000;

    char GENRE_DIVIDER_CHAR = ',';
    String GENRE_DIVIDER = GENRE_DIVIDER_CHAR + " ";

    Set<String> DEFAULT_REMOTE_EXTENSIONS = SetsKt.setOf(
            "mp4a",
            "fmp4",
            "webm",
            "matroska",
            "mp3",
            "m4a",
            "ogg",
            "wav",
            "wma",
            "aac",
            "flac",
            "opus",
            "vorbis",
            "mkv",
            "mp4",
            "ts",
            "3gp"
    );

}
