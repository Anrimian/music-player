package com.github.anrimian.musicplayer.data.database;

import android.content.Context;

import androidx.room.Room;

/**
 * Created on 18.11.2017.
 */

public class DatabaseManager {

    private static final String DATABASE_NAME = "music_player_database";

    private Context context;

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public AppDatabase getAppDatabase() {
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
    }
}
