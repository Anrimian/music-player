package com.github.anrimian.musicplayer.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.anrimian.musicplayer.data.database.mappers.CompositionCorruptionDetector;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;

class Migrations {

    public static Migration getMigration1_2(Context context) {
        return new Migration(1, 2) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `compositions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `artist` TEXT, `title` TEXT, `album` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT)");
                StorageMusicProvider provider = new StorageMusicProvider(context);

                LongSparseArray<StorageComposition> map = provider.getCompositions();
                for(int i = 0, size = map.size(); i < size; i++) {
                    StorageComposition composition = map.valueAt(i);
                    ContentValues cv = new ContentValues();
                    cv.put("artist", composition.getArtist());
                    cv.put("title", composition.getTitle());
                    cv.put("album", composition.getAlbum());
                    cv.put("filePath", composition.getFilePath());
                    cv.put("duration", composition.getDuration());
                    cv.put("size", composition.getSize());
                    cv.put("dateAdded", composition.getDateAdded().getTime());
                    cv.put("dateModified", composition.getDateModified().getTime());
                    cv.put("corruptionType", CompositionCorruptionDetector.getCorruptionType(composition).name());
                    database.insert("compositions", SQLiteDatabase.CONFLICT_REPLACE, cv);
                }

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
                        "SELECT id, (SELECT id FROM compositions WHERE storageId = audioId), position, shuffledPosition " +
                        "FROM play_queue");//select and replace old audio id with new?
                database.execSQL("DROP TABLE play_queue");
                database.execSQL("ALTER TABLE play_queue_new RENAME TO play_queue");

                database.execSQL("CREATE  INDEX `index_play_queue_audioId` ON `play_queue` (`audioId`)");
            }
        };
    }
}
