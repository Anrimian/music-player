
package com.github.anrimian.musicplayer.data.database

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.collection.LongSparseArray
import androidx.core.content.edit
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.room.util.getColumnIndex
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.anrimian.musicplayer.data.database.converters.EnumConverter
import com.github.anrimian.musicplayer.data.database.mappers.CompositionCorruptionDetector
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.M3UEditor
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile
import com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl
import com.github.anrimian.musicplayer.data.storage.providers.FileVolume
import com.github.anrimian.musicplayer.data.storage.providers.music.AudioFileKey
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageAudioFile
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.analytics.NoOpAnalytics
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlaylistFileNameValidator
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import java.io.File
import java.util.LinkedList

@SuppressLint("RestrictedApi")
internal object Migrations {

    val MIGRATION_19_20: Migration = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val availableStatus = LocalFileStatus.AVAILABLE.id

            db.execSQL(
                """
                CREATE TEMP TABLE temp_composition_remap (
                    loser_id INTEGER NOT NULL,
                    survivor_id INTEGER NOT NULL,
                    PRIMARY KEY (loser_id)
                )
                """
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_migration_19_20_dedup ON compositions(folderId, fileName)"
            )
            db.execSQL(
                """
                INSERT INTO temp_composition_remap (loser_id, survivor_id)
                SELECT c.id,
                       (SELECT s.id FROM compositions s
                        WHERE s.folderId = c.folderId AND s.fileName = c.fileName
                        ORDER BY CASE WHEN s.localFileStatus = $availableStatus AND s.storageId IS NOT NULL
                                      THEN 0 ELSE 1 END, s.id
                        LIMIT 1)
                FROM compositions c
                WHERE c.folderId IS NOT NULL
                  AND c.id <> (SELECT s.id FROM compositions s
                        WHERE s.folderId = c.folderId AND s.fileName = c.fileName
                        ORDER BY CASE WHEN s.localFileStatus = $availableStatus AND s.storageId IS NOT NULL
                                      THEN 0 ELSE 1 END, s.id
                        LIMIT 1)
                """
            )

            db.execSQL(
                """
                UPDATE play_lists_entries
                SET audioId = (SELECT survivor_id FROM temp_composition_remap WHERE loser_id = play_lists_entries.audioId)
                WHERE audioId IN (SELECT loser_id FROM temp_composition_remap)
                """
            )
            db.execSQL(
                """
                UPDATE play_queue
                SET audioId = (SELECT survivor_id FROM temp_composition_remap WHERE loser_id = play_queue.audioId)
                WHERE audioId IN (SELECT loser_id FROM temp_composition_remap)
                """
            )
            db.execSQL(
                """
                DELETE FROM genre_entries
                WHERE compositionId IN (SELECT loser_id FROM temp_composition_remap)
                AND EXISTS (
                    SELECT 1
                    FROM genre_entries survivor_entry
                    INNER JOIN temp_composition_remap remap ON remap.loser_id = genre_entries.compositionId
                    WHERE survivor_entry.genreId = genre_entries.genreId
                      AND survivor_entry.compositionId = remap.survivor_id
                )
                """
            )
            db.execSQL(
                """
                UPDATE genre_entries
                SET compositionId = (
                    SELECT survivor_id FROM temp_composition_remap WHERE loser_id = genre_entries.compositionId
                )
                WHERE compositionId IN (SELECT loser_id FROM temp_composition_remap)
                """
            )
            db.execSQL("DELETE FROM compositions WHERE id IN (SELECT loser_id FROM temp_composition_remap)")
            db.execSQL("DROP INDEX IF EXISTS idx_migration_19_20_dedup")
            db.execSQL("DROP TABLE temp_composition_remap")
        }
    }

    fun getMigration18_19(
        context: Context,
        audioCatalogProvider: SystemAudioCatalogProvider,
    ): Migration {

        class VolumeMigrationInfo(val id: Long, val rootFolderId: Long)

        return object : Migration(18, 19) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `volumes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageKey` TEXT NOT NULL, `path` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_volumes_path` ON `volumes` (`path`)")
                db.execSQL("ALTER TABLE `folders` ADD COLUMN `volumeId` INTEGER REFERENCES `volumes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE ")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_folders_volumeId` ON `folders` (`volumeId`)")

                val storageAudioFiles = audioCatalogProvider.getAudioFiles(0, true, Constants.DEFAULT_REMOTE_EXTENSIONS)
                    ?.mapKeys { entry -> entry.value.storageId }

                if (storageAudioFiles.isNullOrEmpty()) {
                    return
                }

                val usedVolumes = storageAudioFiles.values
                    .mapNotNull { file -> FileVolume.fromCanonicalPathOrNull(file.parentPath) }
                    .distinctBy { vol -> vol.path }

                val volumeInfoMap = mutableMapOf<String, VolumeMigrationInfo>()
                for (volume in usedVolumes) {
                    val volumeValues = ContentValues().apply {
                        put("storageKey", volume.storageKey)
                        put("path", volume.path)
                        put("isPrimary", if (volume.isPrimary) 1 else 0)
                    }
                    val volumeId = db.insert("volumes", SQLiteDatabase.CONFLICT_REPLACE, volumeValues)

                    val rootFolderValues = ContentValues().apply {
                        put("name", volume.storageKey)
                        put("volumeId", volumeId)
                    }
                    val rootFolderId = db.insert("folders", SQLiteDatabase.CONFLICT_NONE, rootFolderValues)
                    volumeInfoMap[volume.path] = VolumeMigrationInfo(id = volumeId, rootFolderId = rootFolderId)
                }


                // Compositions and Folders Migration
                // First, get a map of all original root folders (parentId IS NULL).
                // This is crucial for re-parenting them later.
                val currentRootFolders = mutableMapOf<String, Long>()
                db.query("SELECT id, name FROM folders WHERE parentId IS NULL AND volumeId IS NULL").use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow("id")
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        currentRootFolders[cursor.getString(nameIndex)] = cursor.getLong(idIndex)
                    }
                }

                // This recursive query reconstructs the full relative path for each composition.
                val c = db.query("""
                    WITH RECURSIVE folder_path(id, path) AS (
                        SELECT id, name FROM folders WHERE parentId IS NULL
                        UNION ALL
                        SELECT f.id, fp.path || '/' || f.name FROM folders AS f
                        JOIN folder_path AS fp ON f.parentId = fp.id
                    )
                    SELECT c.id AS compositionId, c.storageId, fp.path AS relativePath
                    FROM compositions AS c
                    LEFT JOIN folder_path AS fp ON c.folderId = fp.id
                """)

                val folderCache = mutableMapOf<String, Long>()

                c.use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow("compositionId")
                    val storageIdIndex = cursor.getColumnIndexOrThrow("storageId")
                    val oldPathIndex = cursor.getColumnIndexOrThrow("relativePath")

                    while (cursor.moveToNext()) {
                        val compositionId = cursor.getLong(idIndex)
                        val storageId = cursor.getLong(storageIdIndex)
                        val relativePath = cursor.getString(oldPathIndex)
                        val fullParentPath = storageAudioFiles[storageId]?.parentPath

                        if (fullParentPath != null && fullParentPath != relativePath) {
                            val newFolderId = getOrCreateFolder(
                                db,
                                fullParentPath,
                                folderCache,
                                currentRootFolders,
                                volumeInfoMap
                            )
                            db.execSQL(
                                "UPDATE compositions SET folderId = ? WHERE id = ?",
                                arrayOf(newFolderId, compositionId)
                            )
                        }
                    }
                }

                // Ignored Folders Migration
                val allParentPaths = storageAudioFiles.values
                    .map { audioFile -> audioFile.parentPath }
                    .distinct()

                val configsDb = Room.databaseBuilder(context, ConfigsDatabase::class.java, "configs_database")
                    .addMigrations(MIGRATION_CONFIG_1_2)
                    .build()
                try {
                    val cdb = configsDb.openHelper.writableDatabase
                    cdb.beginTransaction()
                    try {
                        val ignoredCursor = cdb.query("SELECT path FROM ignored_folders")

                        val updates = mutableListOf<Pair<String, String>>()

                        ignoredCursor.use { cursor ->
                            val pathIndex = cursor.getColumnIndexOrThrow("path")
                            while (cursor.moveToNext()) {
                                val oldIgnoredPath = cursor.getString(pathIndex)
                                val newPathMatch = allParentPaths
                                    .filter { path -> path.endsWith(oldIgnoredPath) }
                                    .minByOrNull { path -> path.length }

                                if (newPathMatch != null && newPathMatch != oldIgnoredPath) {
                                    updates.add(Pair(newPathMatch, oldIgnoredPath))
                                }
                            }
                        }

                        updates.forEach { (newPath, oldPath) ->
                            cdb.execSQL(
                                "UPDATE OR REPLACE ignored_folders SET path = ? WHERE path = ?",
                                arrayOf(newPath, oldPath)
                            )
                        }
                        cdb.setTransactionSuccessful()
                    } finally {
                        cdb.endTransaction()
                    }
                } finally {
                    configsDb.close()
                }


                // Normalize playlist entries order position
                // Create a temporary table to store the stable mapping
                db.execSQL("CREATE TEMP TABLE temp_normalized_positions (itemId INTEGER PRIMARY KEY, newPosition INTEGER)")
                // Snapshot the correct dense positions into the temporary table
                db.execSQL("""
                    INSERT INTO temp_normalized_positions (itemId, newPosition)
                    SELECT itemId, (
                        SELECT COUNT(*) 
                        FROM play_lists_entries AS items 
                        WHERE items.playListId = ple.playListId 
                          AND (items.orderPosition < ple.orderPosition OR (items.orderPosition = ple.orderPosition AND items.itemId < ple.itemId))
                    )
                    FROM play_lists_entries AS ple
                """)
                // Update the original table from the static snapshot
                db.execSQL("""
                    UPDATE play_lists_entries 
                    SET orderPosition = (SELECT newPosition FROM temp_normalized_positions WHERE itemId = play_lists_entries.itemId)
                """)
                db.execSQL("DROP TABLE temp_normalized_positions")

                // Migrate playlist file cache
                try {
                    val playlistDir = File(context.filesDir, "playlists")
                    val m3uFiles = playlistDir.listFiles { _, name -> name.endsWith(".m3u") }
                    if (m3uFiles != null && m3uFiles.isNotEmpty()) {
                        val fileNameMap = storageAudioFiles.values.groupBy { storageAudioFile -> storageAudioFile.fileName }
                        val m3uEditor = M3UEditor()
                        for (file in m3uFiles) {
                            try {
                                val playlistFile = file.inputStream().use { stream -> 
                                    m3uEditor.read(PlaylistFileNameValidator.getPlaylistName(file.name), stream) 
                                }
                                var updated = false
                                val newEntries = playlistFile.entries.map { entry ->
                                    val entryPath = entry.filePath
                                    val entryFileName = FileUtils.getFileName(entryPath)
                                    val candidates = fileNameMap[entryFileName]
                                    val match = candidates?.find { candidate ->
                                        val candidateFullPath = candidate.parentPath + "/" + candidate.fileName
                                        candidateFullPath.endsWith(entryPath)
                                    }
                                    if (match != null) {
                                        val fullPath = match.parentPath + "/" + match.fileName
                                        if (fullPath != entryPath) {
                                            updated = true
                                            return@map PlayListEntry(fullPath)
                                        }
                                    }
                                    entry
                                }
                                if (updated) {
                                    val newPlaylistFile = PlayListFile(
                                        playlistFile.name,
                                        playlistFile.createDate,
                                        playlistFile.modifyDate,
                                        newEntries
                                    )
                                    file.outputStream().use { stream -> 
                                        m3uEditor.write(newPlaylistFile, stream) 
                                    }
                                }
                            } catch (_: Exception) {
                                // Skip files that fail to parse
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Fail-safe for the entire cache migration block
                }

                val prefs = context.getSharedPreferences("state_preferences", Context.MODE_PRIVATE)
                prefs.edit { remove("root_folder_path") }

                val settingsPrefs = context.getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
                settingsPrefs.edit {
                    val isBluetoothAutoplayEnabled = context.packageManager
                        .getComponentEnabledSetting(
                            ComponentName(
                                context,
                                "com.github.anrimian.musicplayer.infrastructure.receivers.BluetoothConnectionReceiver"
                            )
                        ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    putBoolean("bluetooth_auto_play", isBluetoothAutoplayEnabled)
                }
            }

            /**
             * A raw SQL implementation to find/create a folder structure,
             * now with logic to correctly re-parent old root folders.
             */
            private fun getOrCreateFolder(
                db: SupportSQLiteDatabase,
                folderPath: String,
                cache: MutableMap<String, Long>,
                unprocessedRootFolders: MutableMap<String, Long>,
                volumeInfoMap: Map<String, VolumeMigrationInfo>,
            ): Long? {
                if (cache.containsKey(folderPath)) {
                    return cache[folderPath]
                }
                val volumePath = volumeInfoMap.keys
                    .filter { p -> folderPath.startsWith(p) }
                    .maxByOrNull { p -> p.length } ?: return null

                val volumeInfo = volumeInfoMap[volumePath]!!
                val volumeId = volumeInfo.id
                val rootFolderId = volumeInfo.rootFolderId

                val relativePath = folderPath.substring(volumePath.length).removePrefix("/")
                if (relativePath.isEmpty()) {
                    cache[folderPath] = rootFolderId
                    return rootFolderId
                }

                val pathSegments = relativePath.split('/')
                var currentParentId = rootFolderId

                for (i in pathSegments.indices) {
                    val segment = pathSegments[i]
                    val currentPath = volumePath + "/" + pathSegments.subList(0, i + 1).joinToString("/")

                    if (cache.containsKey(currentPath)) {
                        currentParentId = cache[currentPath]!!
                        continue
                    }

                    var folderId = findFolderId(db, segment, currentParentId)

                    // If not found, check if it was an old root folder that needs re-parenting.
                    if (folderId == null) {
                        val rootIdToReparent = unprocessedRootFolders[segment]
                        if (rootIdToReparent != null) {
                            db.execSQL(
                                "UPDATE folders SET parentId = ? WHERE id = ?",
                                arrayOf(currentParentId, rootIdToReparent)
                            )
                            folderId = rootIdToReparent
                            unprocessedRootFolders.remove(segment)
                        }
                    }

                    // If still not found, it's a completely new folder, so insert it.
                    if (folderId == null) {
                        val values = ContentValues().apply {
                            put("name", segment)
                            put("parentId", currentParentId)
                            put("volumeId", volumeId)
                        }
                        folderId = db.insert("folders", SQLiteDatabase.CONFLICT_REPLACE, values)
                    }

                    cache[currentPath] = folderId
                    currentParentId = folderId
                }
                return currentParentId
            }

            private fun findFolderId(db: SupportSQLiteDatabase, name: String, parentId: Long): Long? {
                db.query("SELECT id FROM folders WHERE name = ? AND parentId = ?", arrayOf(name, parentId.toString())).use { cursor ->
                    if (cursor.moveToFirst()) {
                        return cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    }
                }
                return null
            }

        }
    }

    val MIGRATION_CONFIG_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE `ignored_folders_tmp` (`path` TEXT NOT NULL, `addTime` INTEGER NOT NULL, PRIMARY KEY(`path`))")
            db.execSQL("INSERT INTO `ignored_folders_tmp` (`path`, `addTime`) SELECT `relativePath`, `addDate` FROM `ignored_folders`")
            db.execSQL("DROP TABLE `ignored_folders`")
            db.execSQL("ALTER TABLE `ignored_folders_tmp` RENAME TO `ignored_folders`")
        }
    }

    val MIGRATION_17_18: Migration = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `title` TEXT, `trackNumber` INTEGER, `discNumber` INTEGER, `comment` TEXT, `lyrics` TEXT, `fileName` TEXT NOT NULL, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `storageId` INTEGER, `addedTime` INTEGER NOT NULL, `modifiedTime` INTEGER NOT NULL, `storageModifyTime` INTEGER NOT NULL, `pathModifyTime` INTEGER, `lastScanTime` INTEGER NOT NULL, `missingTime` INTEGER NOT NULL, `coverModifyTime` INTEGER NOT NULL, `localFileStatus` INTEGER NOT NULL, `corruptionType` INTEGER, `initialSource` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
            // moves values from modifyTime to storageModifyTime and set modifyTime to default(0)
            db.execSQL(
                """
                INSERT INTO `compositions_temp` (
                    id, artistId, albumId, folderId, storageId, title, trackNumber, 
                    discNumber, comment, lyrics, fileName, duration, size, addedTime, 
                    modifiedTime, storageModifyTime, lastScanTime, missingTime, coverModifyTime, localFileStatus, 
                    corruptionType, initialSource
                ) SELECT 
                    id, artistId, albumId, folderId, storageId, title, trackNumber, 
                    discNumber, comment, lyrics, fileName, duration, size, dateAdded, 
                    0, dateModified, lastScanDate, 0, coverModifyTime, ${LocalFileStatus.AVAILABLE.id}, 
                    CASE corruptionType 
                        WHEN 'UNKNOWN' THEN '1'
                        WHEN 'UNSUPPORTED' THEN '2'
                        WHEN 'NOT_FOUND' THEN '3'
                        WHEN 'SOURCE_NOT_FOUND' THEN '4'
                        WHEN 'TOO_LARGE_SOURCE' THEN '5'
                        WHEN 'FILE_IS_CORRUPTED' THEN '6'
                        WHEN 'FILE_READ_TIMEOUT' THEN '7'
                    END, 
                    initialSource FROM compositions
                """
            )

            db.execSQL("DROP TABLE `compositions`")
            db.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`")

            db.execSQL("CREATE INDEX `index_compositions_folderId` ON compositions (`folderId`)")
            db.execSQL("CREATE INDEX `index_compositions_artistId` ON compositions (`artistId`)")
            db.execSQL("CREATE INDEX `index_compositions_albumId` ON compositions (`albumId`)")

            db.execSQL("CREATE TABLE IF NOT EXISTS `folders_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `parentId` INTEGER, `name` TEXT NOT NULL, FOREIGN KEY(`parentId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("INSERT INTO `folders_temp` (id, parentId, name) SELECT id, parentId, IFNULL(name, '') FROM folders")
            db.execSQL("DROP TABLE `folders`")
            db.execSQL("ALTER TABLE `folders_temp` RENAME TO `folders`")
            db.execSQL("CREATE INDEX `index_folders_parentId` ON `folders` (`parentId`)")

            db.execSQL("CREATE TABLE IF NOT EXISTS `albums_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `name` TEXT NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
            db.execSQL("INSERT INTO `albums_temp` (id, artistId, name) SELECT id, artistId, IFNULL(name, '') FROM albums")
            db.execSQL("DROP TABLE `albums`")
            db.execSQL("ALTER TABLE `albums_temp` RENAME TO `albums`")
            db.execSQL("CREATE INDEX `index_albums_artistId` ON `albums` (`artistId`)")
            db.execSQL("CREATE UNIQUE INDEX `index_albums_artistId_name` ON `albums` (`artistId`, `name`)")

            db.execSQL("CREATE TABLE IF NOT EXISTS `artists_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
            db.execSQL("INSERT INTO `artists_temp` (id, name) SELECT id, IFNULL(name, '') FROM artists")
            db.execSQL("DROP TABLE `artists`")
            db.execSQL("ALTER TABLE `artists_temp` RENAME TO `artists`")
            db.execSQL("CREATE UNIQUE INDEX `index_artists_name` ON `artists` (`name`)")

            db.execSQL("CREATE TABLE IF NOT EXISTS `play_lists_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `name` TEXT NOT NULL, `addedTime` INTEGER NOT NULL, `modifiedTime` INTEGER NOT NULL)")
            db.execSQL("INSERT INTO `play_lists_temp` (id, storageId, name, addedTime, modifiedTime) SELECT id, storageId, IFNULL(name, ''), dateAdded, dateModified FROM play_lists")
            db.execSQL("DROP TABLE `play_lists`")
            db.execSQL("ALTER TABLE `play_lists_temp` RENAME TO `play_lists`")
            db.execSQL("CREATE UNIQUE INDEX `index_play_lists_name` ON `play_lists` (`name`)")
        }
    }

    val MIGRATION_16_17: Migration = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `trackNumber` INTEGER, `discNumber` INTEGER, `comment` TEXT, `lyrics` TEXT, `fileName` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `pathModifyTime` INTEGER, `lastScanDate` INTEGER NOT NULL, `coverModifyTime` INTEGER NOT NULL, `corruptionType` TEXT, `initialSource` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
            db.execSQL(
                """
                INSERT INTO `compositions_temp` (
                    id, artistId, albumId, folderId, storageId, title, trackNumber, 
                    discNumber, comment, lyrics, fileName, duration, size, dateAdded, 
                    dateModified, pathModifyTime, lastScanDate, coverModifyTime, corruptionType, 
                    initialSource
                ) SELECT 
                    id, artistId, albumId, folderId, storageId, title, trackNumber, 
                    discNumber, comment, lyrics, fileName, duration, size, dateAdded, 
                    dateModified, NULL, lastScanDate, coverModifyTime, corruptionType, 
                    initialSource FROM compositions
                """
            )

            db.execSQL("DROP TABLE `compositions`")
            db.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`")

            db.execSQL("CREATE INDEX `index_compositions_folderId` ON compositions (`folderId`)")
            db.execSQL("CREATE INDEX `index_compositions_artistId` ON compositions (`artistId`)")
            db.execSQL("CREATE INDEX `index_compositions_albumId` ON compositions (`albumId`)")
        }
    }


    fun getMigration15_16(context: Context): Migration {
        return object : Migration(15, 16) {

            @SuppressLint("UseKtx")
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `track_positions` (`queueItemId` INTEGER NOT NULL, `trackPosition` INTEGER NOT NULL, `writeTime` INTEGER NOT NULL, PRIMARY KEY(`queueItemId`), FOREIGN KEY(`queueItemId`) REFERENCES `play_queue`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

                val prefs = context.getSharedPreferences("ui_preferences", Context.MODE_PRIVATE)
                val itemId = prefs.getLong("current_play_queue_id", UiStateRepositoryImpl.NO_ITEM)
                if (itemId == UiStateRepositoryImpl.NO_ITEM) {
                    return
                }
                val position = prefs.getLong("track_position", 0L)
                if (position != 0L) {
                    db.execSQL("INSERT INTO track_positions (queueItemId, trackPosition, writeTime) VALUES ($itemId, + $position, ${System.currentTimeMillis()})")
                }
                prefs.edit()
                    .putLong("library_genres_position", prefs.getLong("library_agenres_position", 0L))
                    .remove("library_agenres_position")
                    .remove("track_position")
                    .apply()
            }
        }
    }

    val MIGRATION_14_15: Migration = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE `genres`")
            db.execSQL("DROP TABLE `genre_entries`")

            db.execSQL("CREATE TABLE IF NOT EXISTS `genres` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `genre_entries` (`genreId` INTEGER NOT NULL, `compositionId` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`genreId`, `compositionId`), FOREIGN KEY(`compositionId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_genres_name` ON `genres` (`name`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_genre_entries_compositionId` ON `genre_entries` (`compositionId`)")

            db.query("SELECT id, name FROM play_lists").use { c ->
                val cursorWrapper = CursorWrapper(c)
                while (c.moveToNext()) {
                    val name = cursorWrapper.getString("name")
                    if (name.isNullOrEmpty()) {
                        continue
                    }
                    var newName = PlaylistFileNameValidator.getFormattedPlaylistName(name)

                    if (name != newName) {
                        var i = 0
                        while (isPlaylistWithNameExists(db, newName)) {
                            val sb = StringBuilder(newName)
                            sb.setCharAt(0, i.toChar())
                            newName = sb.toString()
                            i++
                        }

                        val id = cursorWrapper.getLong("id")
                        db.execSQL(
                            "UPDATE play_lists SET name = ? WHERE id = ?",
                            arrayOf<Any>(newName, id)
                        )
                    }
                }
            }
        }
    }

    private fun isPlaylistWithNameExists(db: SupportSQLiteDatabase, name: String?): Boolean {
        db.query(
            "SELECT exists(SELECT 1 FROM play_lists WHERE name = ? LIMIT 1)",
            arrayOf<Any?>(name)
        ).use { c ->
            c.moveToNext()
            return c.getInt(0) != 0
        }
    }

    fun getMigration13_14(context: Context): Migration {
        return object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val configsDb = Room.databaseBuilder(context, ConfigsDatabase::class.java, "configs_database")
                    .build()
                try {
                    val cdb = configsDb.openHelper.writableDatabase
                    cdb.beginTransaction()
                    try {
                        db.query("SELECT relativePath, addDate FROM ignored_folders").use { c ->
                            val pathIndex = c.getColumnIndexOrThrow("relativePath")
                            val dateIndex = c.getColumnIndexOrThrow("addDate")

                            while (c.moveToNext()) {
                                val path = c.getString(pathIndex)
                                val date = c.getLong(dateIndex)
                                val cv = ContentValues().apply {
                                    put("relativePath", path)
                                    put("addDate", date)
                                }
                                cdb.insert("ignored_folders", OnConflictStrategy.REPLACE, cv)
                            }
                        }
                        cdb.setTransactionSuccessful()
                    } finally {
                        cdb.endTransaction()
                    }
                } finally {
                    configsDb.close()
                }
                db.execSQL("DROP TABLE ignored_folders")
            }
        }
    }

    val MIGRATION_12_13: Migration = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `trackNumber` INTEGER, `discNumber` INTEGER, `comment` TEXT, `lyrics` TEXT, `fileName` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `lastScanDate` INTEGER NOT NULL, `coverModifyTime` INTEGER NOT NULL, `corruptionType` TEXT, `initialSource` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")

            db.execSQL(
                """
                INSERT INTO `compositions_temp` (
                    id, artistId, albumId, folderId, storageId, title, trackNumber, 
                    discNumber, comment, lyrics, fileName, duration, size, dateAdded, 
                    dateModified, lastScanDate, coverModifyTime, corruptionType,
                    initialSource
                ) SELECT 
                    id, artistId, albumId, folderId, storageId, title, trackNumber, 
                    discNumber, comment, lyrics, fileName, duration, size, dateAdded, 
                    dateModified, lastScanDate, coverModifyTime, corruptionType,
                    initialSource FROM compositions
                """
            )

            db.execSQL("DROP TABLE `compositions`")
            db.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`")

            db.execSQL("CREATE INDEX `index_compositions_folderId` ON compositions (`folderId`)")
            db.execSQL("CREATE INDEX `index_compositions_artistId` ON compositions (`artistId`)")
            db.execSQL("CREATE INDEX `index_compositions_albumId` ON compositions (`albumId`)")
        }
    }

    val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE compositions ADD COLUMN trackNumber INTEGER")
            db.execSQL("ALTER TABLE compositions ADD COLUMN discNumber INTEGER")
            db.execSQL("ALTER TABLE compositions ADD COLUMN comment TEXT")
        }
    }

    val MIGRATION_10_11: Migration = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `albums_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `name` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")

            db.execSQL(
                """
                INSERT INTO `albums_temp` (
                    id, artistId, name
                ) SELECT 
                    id, artistId, name 
                    FROM albums
                """
            )

            db.execSQL("DROP TABLE `albums`")
            db.execSQL("ALTER TABLE `albums_temp` RENAME TO `albums`")

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_albums_artistId` ON `albums` (`artistId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_albums_artistId_name` ON `albums` (`artistId`, `name`)")
        }
    }

    val MIGRATION_9_10: Migration = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE compositions ADD COLUMN coverModifyTime INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `lyrics` TEXT, `fileName` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `lastScanDate` INTEGER NOT NULL, `corruptionType` TEXT, `audioFileType` INTEGER NOT NULL, `initialSource` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")

            db.execSQL(
                """
                INSERT INTO `compositions_temp` (
                    id, artistId, albumId, folderId, storageId, title, lyrics, fileName,
                    duration, size, dateAdded, dateModified, lastScanDate, corruptionType,
                    audioFileType, initialSource
                ) SELECT 
                    id, artistId, albumId, folderId, storageId, title, lyrics, fileName,
                    duration, size, dateAdded, dateModified, lastScanDate, corruptionType,
                    1 AS audioFileType, 1 AS initialSource FROM compositions
                """
            )

            db.execSQL("DROP TABLE `compositions`")
            db.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`")

            db.execSQL("CREATE INDEX `index_compositions_folderId` ON compositions (`folderId`)")
            db.execSQL("CREATE INDEX `index_compositions_artistId` ON compositions (`artistId`)")
            db.execSQL("CREATE INDEX `index_compositions_albumId` ON compositions (`albumId`)")
        }
    }

    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE compositions ADD COLUMN lastScanDate INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE compositions ADD COLUMN lyrics TEXT")
        }
    }

    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE compositions ADD COLUMN fileName TEXT")

            db.query("SELECT id, filePath FROM compositions").use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(getColumnIndex(c, "id"))
                    val filePath = c.getString(getColumnIndex(c, "filePath"))

                    val fileName = FileUtils.formatFileName(filePath, true)

                    val cv = ContentValues()
                    cv.put("fileName", fileName)

                    db.update(
                        "compositions",
                        SQLiteDatabase.CONFLICT_REPLACE,
                        cv,
                        "id = ?",
                        arrayOf(id.toString())
                    )
                }
            }
        }
    }

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `ignored_folders` (`relativePath` TEXT NOT NULL, `addDate` INTEGER, PRIMARY KEY(`relativePath`))")

            db.execSQL("CREATE TABLE IF NOT EXISTS `folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `parentId` INTEGER, `name` TEXT, FOREIGN KEY(`parentId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX `index_folders_parentId` ON `folders` (`parentId`)")

            //migrate compositions
            db.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")

            db.query("SELECT * FROM compositions").use { c ->
                val cursorWrapper = CursorWrapper(c)
                while (c.moveToNext()) {
                    val cv = ContentValues()

                    cv.put("id", cursorWrapper.getLong("id"))
                    cv.put("artistId", cursorWrapper.getLong("artistId"))
                    cv.put("albumId", cursorWrapper.getLong("albumId"))
                    cv.put("storageId", cursorWrapper.getLong("storageId"))
                    cv.put("title", cursorWrapper.getString("title"))
                    cv.put("filePath", cursorWrapper.getString("filePath"))
                    cv.put("duration", cursorWrapper.getLong("duration"))
                    cv.put("size", cursorWrapper.getLong("size"))
                    cv.put("dateAdded", cursorWrapper.getLong("dateAdded"))
                    cv.put("dateModified", cursorWrapper.getLong("dateModified"))
                    cv.put("corruptionType", cursorWrapper.getString("corruptionType"))
                    db.insert("compositions_temp", SQLiteDatabase.CONFLICT_REPLACE, cv)
                }

                db.execSQL("DROP TABLE `compositions`")
                db.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`")

                db.execSQL("CREATE INDEX `index_compositions_folderId` ON compositions (`folderId`)")
                db.execSQL("CREATE INDEX `index_compositions_artistId` ON compositions (`artistId`)")
                db.execSQL("CREATE INDEX `index_compositions_albumId` ON compositions (`albumId`)")
            }
        }
    }

    private fun getLong(c: android.database.Cursor, columnName: String): Long? {
        val columnIndex = getColumnIndex(c, columnName)
        return if (columnIndex < 0 || c.isNull(columnIndex)) {
            null
        } else {
            c.getLong(columnIndex)
        }
    }

    fun getMigration3_4(): Migration {
        return object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS artists (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)")
                db.execSQL("CREATE UNIQUE INDEX `index_artists_name` ON artists (`name`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS albums (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `name` TEXT, `firstYear` INTEGER NOT NULL, `lastYear` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
                db.execSQL("CREATE INDEX `index_albums_artistId` ON albums (`artistId`)")
                db.execSQL("CREATE UNIQUE INDEX `index_albums_artistId_name` ON albums (`artistId`, `name`)")

                //compositions
                db.execSQL("CREATE TABLE IF NOT EXISTS compositions_temp (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `storageId` INTEGER, `title` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")

                val artistCache = HashMap<String?, Long?>()
                HashMap<String?, Long?>()
                db.query("SELECT * FROM compositions").use { c ->
                    val cursorWrapper = CursorWrapper(c)
                    while (c.moveToNext()) {
                        val cv = ContentValues()

                        //artists
                        val artist = cursorWrapper.getString("artist")
                        val artistId = insertArtist(artist, db, artistCache)
                        cv.put("artistId", artistId)

                        val storageId = cursorWrapper.getLong("storageId")

                        cv.put("id", cursorWrapper.getLong("id"))
                        cv.put("storageId", storageId)
                        cv.put("title", cursorWrapper.getString("title"))
                        cv.put("filePath", cursorWrapper.getString("filePath"))
                        cv.put("duration", cursorWrapper.getLong("duration"))
                        cv.put("size", cursorWrapper.getLong("size"))
                        cv.put("dateAdded", cursorWrapper.getLong("dateAdded"))
                        cv.put("dateModified", cursorWrapper.getLong("dateModified"))
                        cv.put("corruptionType", cursorWrapper.getString("corruptionType"))
                        db.insert("compositions_temp", SQLiteDatabase.CONFLICT_REPLACE, cv)
                    }
                }
                db.execSQL("DROP TABLE compositions")
                db.execSQL("ALTER TABLE compositions_temp RENAME TO compositions")
                db.execSQL("CREATE INDEX `index_compositions_artistId` ON compositions (`artistId`)")
                db.execSQL("CREATE INDEX `index_compositions_albumId` ON compositions (`albumId`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS genres (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `name` TEXT)")
                db.execSQL("CREATE UNIQUE INDEX `index_genres_name` ON genres (`name`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS genre_entries (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `audioId` INTEGER NOT NULL, `genreId` INTEGER NOT NULL, `storageId` INTEGER, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX `index_genre_entries_audioId` ON genre_entries (`audioId`)")
                db.execSQL("CREATE INDEX `index_genre_entries_genreId` ON genre_entries (`genreId`)")
            }
        }
    }

    private fun insertArtist(
        artist: String?,
        db: SupportSQLiteDatabase,
        artistCache: MutableMap<String?, Long?>,
    ): Long? {
        var artistId: Long? = null
        if (artist != null) {
            artistId = artistCache[artist]
            if (artistId == null) {
                val cvArt = ContentValues()
                cvArt.put("name", artist)
                artistId = db.insert("artists", SQLiteDatabase.CONFLICT_REPLACE, cvArt)
                artistCache[artist] = artistId
            }
        }
        return artistId
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            //copy values with verified index
            val positionMap = LongSparseArray<Int>()
            db.query("SELECT id FROM play_queue ORDER BY position").use { c ->
                val cursorWrapper = CursorWrapper(c)
                for (i in 0 until c.count) {
                    c.moveToPosition(i)
                    positionMap.put(cursorWrapper.getLong("id"), i)
                }
            }
            val cvList = LinkedList<ContentValues>()
            db.query("SELECT id, audioId FROM play_queue ORDER BY shuffledPosition")
                .use { c ->
                    val cursorWrapper = CursorWrapper(c)
                    for (i in 0 until c.count) {
                        c.moveToPosition(i)
                        val cv = ContentValues()
                        val id = cursorWrapper.getLong("id")
                        cv.put("id", id)
                        cv.put("audioId", cursorWrapper.getLong("audioId"))
                        cv.put("position", positionMap.get(id))
                        cv.put("shuffledPosition", i)
                        cvList.add(cv)
                    }
                }
            db.execSQL("DELETE FROM play_queue")
            for (cv in cvList) {
                db.insert("play_queue", SQLiteDatabase.CONFLICT_REPLACE, cv)
            }

            db.execSQL("CREATE UNIQUE INDEX `index_play_queue_position` ON `play_queue` (`position`)")
            db.execSQL("CREATE UNIQUE INDEX `index_play_queue_shuffledPosition` ON `play_queue` (`shuffledPosition`)")
        }
    }

    fun getMigration1_2(context: Context): Migration {
        return object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `compositions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `artist` TEXT, `title` TEXT, `album` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT)")
                val provider = SystemAudioCatalogProvider(context, NoOpAnalytics)

                val enumConverter = EnumConverter()
                val map: Map<AudioFileKey, StorageAudioFile> = try {
                    provider.getAudioFiles(
                        minAudioDurationMillis = 0,
                        showAllAudioFiles = false,
                        allowedExtensions = Constants.DEFAULT_REMOTE_EXTENSIONS
                    ) ?: HashMap()
                } catch (_: Exception) { // quick fix for old apis
                    HashMap()
                }

                for (composition in map.values) {
                    val cv = ContentValues()
                    cv.put("storageId", composition.storageId)
                    cv.put("artist", composition.artist)
                    cv.put("title", composition.title)
                    cv.put("filePath", composition.parentPath)
                    cv.put("duration", composition.duration)
                    cv.put("size", composition.size)
                    cv.put("dateAdded", composition.addedTime)
                    cv.put("dateModified", composition.modifiedTime)
                    cv.put(
                        "corruptionType",
                        enumConverter.toId(
                            CompositionCorruptionDetector.getCorruptionType(composition.duration)
                        )
                    )
                    db.insert("compositions", SQLiteDatabase.CONFLICT_REPLACE, cv)
                }

                //playlists
                db.execSQL("CREATE TABLE IF NOT EXISTS `play_lists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `name` TEXT, `dateAdded` INTEGER, `dateModified` INTEGER)")
                db.execSQL("CREATE UNIQUE INDEX `index_play_lists_name` ON `play_lists` (`name`)")

                //play lists entries
                db.execSQL("CREATE TABLE IF NOT EXISTS `play_lists_entries` (`itemId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageItemId` INTEGER, `audioId` INTEGER NOT NULL, `playListId` INTEGER NOT NULL, `orderPosition` INTEGER NOT NULL, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`playListId`) REFERENCES `play_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX `index_play_lists_entries_audioId` ON `play_lists_entries` (`audioId`)")
                db.execSQL("CREATE INDEX `index_play_lists_entries_playListId` ON `play_lists_entries` (`playListId`)")

                //play queue
                db.execSQL("CREATE TABLE IF NOT EXISTS `play_queue_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `audioId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `shuffledPosition` INTEGER NOT NULL, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.query("SELECT id, (SELECT id FROM compositions WHERE storageId = audioId), position, shuffledPosition FROM play_queue")
                    .use { c ->
                        while (c.moveToNext()) {
                            val cv = ContentValues()
                            cv.put("id", getLong(c, "id"))
                            val audioId = getLong(c, "audioId")
                            if (audioId == null || audioId < 1) {
                                continue
                            }
                            cv.put("audioId", audioId)
                            cv.put("position", getLong(c, "position"))
                            cv.put("shuffledPosition", getLong(c, "shuffledPosition"))
                            db.insert("play_queue_new", SQLiteDatabase.CONFLICT_REPLACE, cv)
                        }
                    }
                db.execSQL(
                    """
                    INSERT INTO `play_queue_new` (id, audioId, position, shuffledPosition) 
                    SELECT id, (SELECT id FROM compositions WHERE storageId = audioId), position, shuffledPosition 
                    FROM play_queue
                    """
                ) //select and replace old audio id with new?
                db.execSQL("DROP TABLE play_queue")
                db.execSQL("ALTER TABLE play_queue_new RENAME TO play_queue")

                db.execSQL("CREATE INDEX `index_play_queue_audioId` ON `play_queue` (`audioId`)")
            }
        }
    }

}
