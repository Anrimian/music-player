package com.github.anrimian.musicplayer.data.database;

import android.content.Context;

import androidx.room.Room;

/**
 * Created on 18.11.2017.
 */

public class DatabaseManager {

    private static final String LIBRARY_DATABASE_NAME = "music_player_database";
    private static final String CONFIGS_DATABASE_NAME = "configs_database";

    private final Context context;

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public LibraryDatabase getLibraryDatabase() {
        return Room.databaseBuilder(context, LibraryDatabase.class, LIBRARY_DATABASE_NAME)
                .addMigrations(Migrations.getMigration1_2(context),
                        Migrations.MIGRATION_2_3,
                        Migrations.getMigration3_4(context),
                        Migrations.MIGRATION_4_5,
                        Migrations.MIGRATION_5_6,
                        Migrations.MIGRATION_6_7,
                        Migrations.MIGRATION_7_8,
                        Migrations.MIGRATION_8_9,
                        Migrations.MIGRATION_9_10,
                        Migrations.MIGRATION_10_11,
                        Migrations.MIGRATION_11_12,
                        Migrations.MIGRATION_12_13,
                        Migrations.getMigration13_14(context),
                        Migrations.MIGRATION_14_15,
                        Migrations.getMigration15_16(context))
                .build();
    }

    public ConfigsDatabase getConfigsDatabase() {
        return Room.databaseBuilder(context, ConfigsDatabase.class, CONFIGS_DATABASE_NAME).build();
    }

}
