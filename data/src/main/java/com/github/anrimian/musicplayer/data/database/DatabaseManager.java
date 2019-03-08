package com.github.anrimian.musicplayer.data.database;

import android.content.Context;

import androidx.room.Room;

import static com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_1_2;

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
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .build();
    }
}
