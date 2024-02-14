package com.github.anrimian.musicplayer.data.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.migration.Migration;
import androidx.room.util.CursorUtil;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.anrimian.musicplayer.data.database.converters.EnumConverter;
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionCorruptionDetector;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.utils.Permissions;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlayListFileNameValidator;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@SuppressLint("RestrictedApi")
class Migrations {

    static Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE `genres`");
            database.execSQL("DROP TABLE `genre_entries`");

            database.execSQL("CREATE TABLE IF NOT EXISTS `genres` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `genre_entries` (`genreId` INTEGER NOT NULL, `compositionId` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`genreId`, `compositionId`), FOREIGN KEY(`compositionId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");

            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_genres_name` ON `genres` (`name`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_genre_entries_compositionId` ON `genre_entries` (`compositionId`)");

            try (Cursor c = database.query("SELECT id, name FROM play_lists")) {
                CursorWrapper cursorWrapper = new CursorWrapper(c);
                while (c.moveToNext()) {
                    String name = cursorWrapper.getString("name");
                    if (name == null) {
                        continue;
                    }
                    String newName = PlayListFileNameValidator.INSTANCE.getFormattedPlaylistName(name);

                    if (!name.equals(newName)) {
                        int i = 0;

                        while (isPlaylistWithNameExists(database, newName)) {
                            StringBuilder sb = new StringBuilder(newName);
                            sb.setCharAt(0, (char) i);
                            newName = sb.toString();
                        }

                        long id = cursorWrapper.getLong("id");
                        database.execSQL("UPDATE play_lists SET name = ? WHERE id = ?", new Object[] { newName, id });
                    }
                }
            }
        }
    };

    private static boolean isPlaylistWithNameExists(SupportSQLiteDatabase database, String name) {
        try (Cursor c = database.query("SELECT exists(SELECT 1 FROM play_lists WHERE name = ? LIMIT 1)", new Object[] { name })) {
            c.moveToNext();
            return c.getInt(0) != 0;
        }
    }

    static Migration getMigration13_14(Context context) {
        return new Migration(13, 14) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                DatabaseManager databaseManager = new DatabaseManager(context);
                IgnoredFoldersDao dao = databaseManager.getConfigsDatabase().ignoredFoldersDao();

                try (Cursor c = database.query("SELECT relativePath, addDate FROM ignored_folders")) {
                    CursorWrapper cursorWrapper = new CursorWrapper(c);
                    while (c.moveToNext()) {
                        String path = cursorWrapper.getString("relativePath");
                        long date = cursorWrapper.getLong("addDate");
                        //noinspection ConstantConditions
                        dao.insert(path, new Date(date));
                    }
                }
                database.execSQL("DROP TABLE ignored_folders");
            }
        };
    }

    static Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `trackNumber` INTEGER, `discNumber` INTEGER, `comment` TEXT, `lyrics` TEXT, `fileName` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `lastScanDate` INTEGER NOT NULL, `coverModifyTime` INTEGER NOT NULL, `corruptionType` TEXT, `initialSource` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");

            database.execSQL(
                    "INSERT INTO `compositions_temp` (" +
                            "id, artistId, albumId, folderId, storageId, title, trackNumber, " +
                            "discNumber, comment, lyrics, fileName, duration, size, dateAdded, " +
                            "dateModified, lastScanDate, coverModifyTime, corruptionType," +
                            "initialSource" +
                            ") SELECT " +
                            "id, artistId, albumId, folderId, storageId, title, trackNumber, " +
                            "discNumber, comment, lyrics, fileName, duration, size, dateAdded, " +
                            "dateModified, lastScanDate, coverModifyTime, corruptionType," +
                            "initialSource FROM compositions"
            );

            database.execSQL("DROP TABLE `compositions`");
            database.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`");

            database.execSQL("CREATE  INDEX `index_compositions_folderId` ON compositions (`folderId`)");
            database.execSQL("CREATE  INDEX `index_compositions_artistId` ON compositions (`artistId`)");
            database.execSQL("CREATE  INDEX `index_compositions_albumId` ON compositions (`albumId`)");
        }
    };

    static Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE compositions ADD COLUMN trackNumber INTEGER");
            database.execSQL("ALTER TABLE compositions ADD COLUMN discNumber INTEGER");
            database.execSQL("ALTER TABLE compositions ADD COLUMN comment TEXT");
        }
    };

    static Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `albums_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `name` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");

            database.execSQL(
                    "INSERT INTO `albums_temp` (" +
                            "id, artistId, name" +
                            ") SELECT " +
                            "id, artistId, name " +
                            "FROM albums"
            );

            database.execSQL("DROP TABLE `albums`");
            database.execSQL("ALTER TABLE `albums_temp` RENAME TO `albums`");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_albums_artistId` ON `albums` (`artistId`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_albums_artistId_name` ON `albums` (`artistId`, `name`)");
        }
    };

    static Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE compositions ADD COLUMN coverModifyTime INTEGER NOT NULL DEFAULT 0");
        }
    };

    static Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `lyrics` TEXT, `fileName` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `lastScanDate` INTEGER NOT NULL, `corruptionType` TEXT, `audioFileType` INTEGER NOT NULL, `initialSource` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");

            database.execSQL(
                    "INSERT INTO `compositions_temp` (" +
                            "id, artistId, albumId, folderId, storageId, title, lyrics, fileName," +
                            "duration, size, dateAdded, dateModified, lastScanDate, corruptionType," +
                            "audioFileType, initialSource" +
                            ") SELECT " +
                            "id, artistId, albumId, folderId, storageId, title, lyrics, fileName," +
                            "duration, size, dateAdded, dateModified, lastScanDate, corruptionType," +
                            "1 AS audioFileType, 1 AS initialSource FROM compositions"
            );

            database.execSQL("DROP TABLE `compositions`");
            database.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`");

            database.execSQL("CREATE  INDEX `index_compositions_folderId` ON compositions (`folderId`)");
            database.execSQL("CREATE  INDEX `index_compositions_artistId` ON compositions (`artistId`)");
            database.execSQL("CREATE  INDEX `index_compositions_albumId` ON compositions (`albumId`)");
        }
    };

    static Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE compositions ADD COLUMN lastScanDate INTEGER NOT NULL DEFAULT 0");
        }
    };

    static Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE compositions ADD COLUMN lyrics TEXT");
        }
    };

    static Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE compositions ADD COLUMN fileName TEXT");

            //migrate file name
            try (Cursor c = database.query("SELECT id, filePath FROM compositions")) {
                while (c.moveToNext()) {
                    long id = c.getLong(CursorUtil.getColumnIndex(c, "id"));
                    String filePath = c.getString(CursorUtil.getColumnIndex(c, "filePath"));

                    String fileName = FileUtils.formatFileName(filePath, true);

                    ContentValues cv = new ContentValues();
                    cv.put("fileName", fileName);

                    database.update("compositions",
                            SQLiteDatabase.CONFLICT_REPLACE,
                            cv,
                            "id = ?",
                            new String[]{String.valueOf(id)}
                    );
                }
            }
        }
    };

    static Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ignored_folders` (`relativePath` TEXT NOT NULL, `addDate` INTEGER, PRIMARY KEY(`relativePath`))");

            database.execSQL("CREATE TABLE IF NOT EXISTS `folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `parentId` INTEGER, `name` TEXT, FOREIGN KEY(`parentId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE  INDEX `index_folders_parentId` ON `folders` (`parentId`)");

            //migrate compositions
            database.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");

            //don't migrate folders, they will update automatically on next storage rescan
            try (Cursor c = database.query("SELECT * FROM compositions")) {
                CursorWrapper cursorWrapper = new CursorWrapper(c);
                while (c.moveToNext()) {
                    ContentValues cv = new ContentValues();

                    cv.put("id", cursorWrapper.getLong( "id"));
                    cv.put("artistId", cursorWrapper.getLong("artistId"));
                    cv.put("albumId", cursorWrapper.getLong( "albumId"));
                    cv.put("storageId", cursorWrapper.getLong("storageId"));
                    cv.put("title", cursorWrapper.getString("title"));
                    cv.put("filePath", cursorWrapper.getString("filePath"));
                    cv.put("duration", cursorWrapper.getLong( "duration"));
                    cv.put("size", cursorWrapper.getLong( "size"));
                    cv.put("dateAdded", cursorWrapper.getLong("dateAdded"));
                    cv.put("dateModified", cursorWrapper.getLong("dateModified"));
                    cv.put("corruptionType", cursorWrapper.getString("corruptionType"));
                    database.insert("compositions_temp", SQLiteDatabase.CONFLICT_REPLACE, cv);
                }

                database.execSQL("DROP TABLE `compositions`");
                database.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`");

                database.execSQL("CREATE  INDEX `index_compositions_folderId` ON compositions (`folderId`)");
                database.execSQL("CREATE  INDEX `index_compositions_artistId` ON compositions (`artistId`)");
                database.execSQL("CREATE  INDEX `index_compositions_albumId` ON compositions (`albumId`)");
            }
        }
    };

    @SuppressLint("RestrictedApi")
    private static Long getLong(Cursor c, String columnName) {
        int columnIndex = CursorUtil.getColumnIndex(c, columnName);
        if (columnIndex < 0 || c.isNull(columnIndex)) {
            return null;
        } else {
            return c.getLong(columnIndex);
        }
    }

    static Migration getMigration3_4(Context context) {
        return new Migration(3, 4) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS artists (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)");
                database.execSQL("CREATE UNIQUE INDEX `index_artists_name` ON artists (`name`)");

                database.execSQL("CREATE TABLE IF NOT EXISTS albums (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `name` TEXT, `firstYear` INTEGER NOT NULL, `lastYear` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
                database.execSQL("CREATE  INDEX `index_albums_artistId` ON albums (`artistId`)");
                database.execSQL("CREATE UNIQUE INDEX `index_albums_artistId_name` ON albums (`artistId`, `name`)");

                //compositions
                database.execSQL("CREATE TABLE IF NOT EXISTS compositions_temp (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `storageId` INTEGER, `title` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");

                StorageAlbumsProvider storageAlbumsProvider = new StorageAlbumsProvider(context);
                StorageMusicProvider provider = new StorageMusicProvider(context, storageAlbumsProvider);
                LongSparseArray<StorageFullComposition> storageCompositions;
                if (Permissions.hasFilePermission(context)) {
                    storageCompositions = provider.getCompositions(0, false);
                    if (storageCompositions == null) {
                        storageCompositions = new LongSparseArray<>();
                    }
                } else {
                    storageCompositions = new LongSparseArray<>();
                }

                Map<String, Long> artistCache = new HashMap<>();
                Map<String, Long> albumsCache = new HashMap<>();
                try (Cursor c = database.query("SELECT * FROM compositions")) {
                    CursorWrapper cursorWrapper = new CursorWrapper(c);
                    while (c.moveToNext()) {
                        ContentValues cv = new ContentValues();

                        //artists
                        String artist = cursorWrapper.getString("artist");
                        Long artistId = insertArtist(artist, database, artistCache);
                        cv.put("artistId", artistId);

                        long storageId = cursorWrapper.getLong("storageId");

                        //albums
                        Long albumId = null;
                        if (storageId != 0) {
                            StorageFullComposition composition = storageCompositions.get(storageId);
                            if (composition != null) {
                                StorageAlbum album = composition.getStorageAlbum();
                                if (album != null) {
                                    Long albumArtistId = insertArtist(album.getArtist(), database, artistCache);
                                    albumId = insertAlbum(album, albumArtistId, database, albumsCache);
                                }
                            }
                        }
                        cv.put("albumId", albumId);

                        cv.put("id", cursorWrapper.getLong("id"));
                        cv.put("storageId", storageId);
                        cv.put("title", cursorWrapper.getString("title"));
                        cv.put("filePath", cursorWrapper.getString("filePath"));
                        cv.put("duration", cursorWrapper.getLong("duration"));
                        cv.put("size", cursorWrapper.getLong("size"));
                        cv.put("dateAdded", cursorWrapper.getLong("dateAdded"));
                        cv.put("dateModified", cursorWrapper.getLong("dateModified"));
                        cv.put("corruptionType", cursorWrapper.getString("corruptionType"));
                        database.insert("compositions_temp", SQLiteDatabase.CONFLICT_REPLACE, cv);
                    }
                }

                database.execSQL("DROP TABLE compositions");
                database.execSQL("ALTER TABLE compositions_temp RENAME TO compositions");
                database.execSQL("CREATE  INDEX `index_compositions_artistId` ON compositions (`artistId`)");
                database.execSQL("CREATE  INDEX `index_compositions_albumId` ON compositions (`albumId`)");

                database.execSQL("CREATE TABLE IF NOT EXISTS genres (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `name` TEXT)");
                database.execSQL("CREATE UNIQUE INDEX `index_genres_name` ON genres (`name`)");
                database.execSQL("CREATE TABLE IF NOT EXISTS genre_entries (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `audioId` INTEGER NOT NULL, `genreId` INTEGER NOT NULL, `storageId` INTEGER, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
                database.execSQL("CREATE  INDEX `index_genre_entries_audioId` ON genre_entries (`audioId`)");
                database.execSQL("CREATE  INDEX `index_genre_entries_genreId` ON genre_entries (`genreId`)");
            }
        };
    }

    @Nullable
    private static Long insertArtist(String artist,
                                     SupportSQLiteDatabase database,
                                     Map<String, Long> artistCache) {
        Long artistId = null;
        if (artist != null) {
            artistId = artistCache.get(artist);
            if (artistId == null) {
                ContentValues cvArt = new ContentValues();
                cvArt.put("name", artist);
                artistId = database.insert("artists", SQLiteDatabase.CONFLICT_REPLACE, cvArt);
                artistCache.put(artist, artistId);
            }
        }
        return artistId;
    }

    private static Long insertAlbum(StorageAlbum album,
                                    Long albumArtistId,
                                    SupportSQLiteDatabase database,
                                    Map<String, Long> albumsCache) {
        String albumName = album.getAlbum();

        Long albumId = albumsCache.get(albumName);
        if (albumId != null) {
            return albumId;
        }

        ContentValues cvAlb = new ContentValues();
        cvAlb.put("artistId", albumArtistId);
        cvAlb.put("name", album.getAlbum());
        cvAlb.put("firstYear", 0);
        cvAlb.put("lastYear", 0);

        albumId = database.insert("albums", SQLiteDatabase.CONFLICT_REPLACE, cvAlb);

        albumsCache.put(albumName, albumId);
        return albumId;
    }

    static Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //copy values with verified index
            LongSparseArray<Integer> positionMap = new LongSparseArray<>();
            try (Cursor c = database.query("SELECT id FROM play_queue ORDER BY position")) {
                CursorWrapper cursorWrapper = new CursorWrapper(c);
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    positionMap.put(cursorWrapper.getLong("id"), i);
                }
            }

            LinkedList<ContentValues> cvList = new LinkedList<>();
            try (Cursor c = database.query("SELECT id, audioId FROM play_queue ORDER BY shuffledPosition")) {
                CursorWrapper cursorWrapper = new CursorWrapper(c);
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    ContentValues cv = new ContentValues();
                    long id = cursorWrapper.getLong("id");
                    cv.put("id", id);
                    cv.put("audioId", cursorWrapper.getLong("audioId"));
                    cv.put("position", positionMap.get(id));
                    cv.put("shuffledPosition", i);
                    cvList.add(cv);
                }
            }

            database.execSQL("DELETE FROM play_queue");
            for (ContentValues cv: cvList) {
                database.insert("play_queue", SQLiteDatabase.CONFLICT_REPLACE, cv);
            }

            database.execSQL("CREATE UNIQUE INDEX `index_play_queue_position` ON `play_queue` (`position`)");
            database.execSQL("CREATE UNIQUE INDEX `index_play_queue_shuffledPosition` ON `play_queue` (`shuffledPosition`)");
        }
    };

    static Migration getMigration1_2(Context context) {
        return new Migration(1, 2) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `compositions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `artist` TEXT, `title` TEXT, `album` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT)");
                StorageAlbumsProvider albumsProvider = new StorageAlbumsProvider(context);
                StorageMusicProvider provider = new StorageMusicProvider(context, albumsProvider);

                EnumConverter enumConverter = new EnumConverter();
                LongSparseArray<StorageFullComposition> map = provider.getCompositions(0, false);
                if (map == null) {
                    map = new LongSparseArray<>();
                }
                for(int i = 0, size = map.size(); i < size; i++) {
                    StorageFullComposition composition = map.valueAt(i);
                    ContentValues cv = new ContentValues();
                    cv.put("storageId", composition.getStorageId());
                    cv.put("artist", composition.getArtist());
                    cv.put("title", composition.getTitle());
                    StorageAlbum storageAlbum = composition.getStorageAlbum();
                    if (storageAlbum != null) {
                        cv.put("album", storageAlbum.getAlbum());
                    }
                    cv.put("filePath", composition.getRelativePath());
                    cv.put("duration", composition.getDuration());
                    cv.put("size", composition.getSize());
                    cv.put("dateAdded", composition.getDateAdded().getTime());
                    cv.put("dateModified", composition.getDateModified().getTime());
                    cv.put("corruptionType", enumConverter.toName(CompositionCorruptionDetector.getCorruptionType(composition.getDuration())));
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
                try (Cursor c = database.query("SELECT id, (SELECT id FROM compositions WHERE storageId = audioId), position, shuffledPosition FROM play_queue")) {
                    while (c.moveToNext()) {
                        ContentValues cv = new ContentValues();
                        cv.put("id", getLong(c, "id"));
                        Long audioId = getLong(c, "audioId");
                        if (audioId == null || audioId < 1) {
                            continue;
                        }
                        cv.put("audioId", audioId);
                        cv.put("position", getLong(c, "position"));
                        cv.put("shuffledPosition", getLong(c, "shuffledPosition"));
                        database.insert("play_queue_new", SQLiteDatabase.CONFLICT_REPLACE, cv);
                    }
                }
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
