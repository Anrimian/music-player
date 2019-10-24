package com.github.anrimian.musicplayer.data.database;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

class Migrations {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `compositions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `artist` TEXT, `title` TEXT, `album` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT)");

            //playlists
            database.execSQL("CREATE TABLE IF NOT EXISTS `play_lists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `name` TEXT, `dateAdded` INTEGER, `dateModified` INTEGER)");
            database.execSQL("CREATE UNIQUE INDEX `index_play_lists_name` ON `play_lists` (`name`)");

            //play lists entries
            database.execSQL("CREATE TABLE IF NOT EXISTS `play_lists_entries` (`itemId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageItemId` INTEGER, `audioId` INTEGER NOT NULL, `playListId` INTEGER NOT NULL, `orderPosition` INTEGER NOT NULL, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`playListId`) REFERENCES `play_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX `index_play_lists_entries_audioId` ON `play_lists_entries` (`audioId`)");
            database.execSQL("CREATE INDEX `index_play_lists_entries_playListId` ON `play_lists_entries` (`playListId`)");

            //play queue
            database.execSQL("CREATE TABLE IF NOT EXISTS `play_queue_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `audioId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `shuffledPosition` INTEGER NOT NULL, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("INSERT INTO `play_queue_new` (id, audioId, position, shuffledPosition) " +
                    "SELECT id, audioId, position, shuffledPosition " +
                    "FROM play_queue");//select and replace old audio id with new?
            database.execSQL("DROP TABLE play_queue");
            database.execSQL("ALTER TABLE play_queue_new RENAME TO play_queue");

            database.execSQL("CREATE  INDEX `index_play_queue_audioId` ON `play_queue` (`audioId`)");
        }
    };
}
