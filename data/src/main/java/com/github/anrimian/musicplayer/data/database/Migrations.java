package com.github.anrimian.musicplayer.data.database;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

class Migrations {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE UNIQUE INDEX index_play_queue_position ON play_queue (position)"
            );
            database.execSQL(
                    "CREATE UNIQUE INDEX index_play_queue_shuffledPosition ON play_queue (shuffledPosition)"
            );
        }
    };
}
