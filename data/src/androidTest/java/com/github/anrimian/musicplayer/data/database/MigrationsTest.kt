package com.github.anrimian.musicplayer.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_10_11
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_11_12
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_12_13
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_14_15
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_16_17
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_17_18
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_19_20
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_2_3
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_4_5
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_5_6
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_6_7
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_7_8
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_8_9
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_9_10
import com.github.anrimian.musicplayer.data.database.Migrations.MIGRATION_CONFIG_1_2
import com.github.anrimian.musicplayer.data.database.Migrations.getMigration13_14
import com.github.anrimian.musicplayer.data.database.Migrations.getMigration15_16
import com.github.anrimian.musicplayer.data.database.Migrations.getMigration1_2
import com.github.anrimian.musicplayer.data.database.Migrations.getMigration3_4
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.M3UEditor
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile
import com.github.anrimian.musicplayer.data.storage.providers.music.AudioFileKey
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.data.utils.TestDataProvider.createFakeStorageFile
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlaylistFileNameValidator
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import java.io.File

class MigrationsTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.context

    private val testHelper = MigrationTestHelper(instrumentation, LibraryDatabase::class.java)

    @Test
    fun testMigrationFrom19To20() {
        val folderId = 10L
        val fileName = "Rose Betts.mp3"
        val available = LocalFileStatus.AVAILABLE.id
        val disappeared = LocalFileStatus.DISAPPEARED.id
        val libraryEntryOnly = LocalFileStatus.LIBRARY_ENTRY_ONLY.id

        testHelper.createDatabase(TEST_DB_NAME, 19).apply {
            execSQL("INSERT INTO folders (id, parentId, name) VALUES ($folderId, NULL, 'Rose Betts')")
            execSQL("INSERT INTO play_lists (id, name, addedTime, modifiedTime) VALUES (1, 'Test Playlist', 0, 0)")
            execSQL("INSERT INTO genres (id, name) VALUES (1, 'Rock')")
            execSQL("INSERT INTO genres (id, name) VALUES (2, 'Pop')")

            // Eight duplicate rows for the same (folderId, fileName); id=3 should survive.
            val duplicateRows = listOf(
                Triple(1L, null, disappeared),
                Triple(2L, 200L, libraryEntryOnly),
                Triple(3L, 100L, available),
                Triple(4L, 101L, disappeared),
                Triple(5L, 100L, available),
                Triple(6L, null, libraryEntryOnly),
                Triple(7L, 102L, disappeared),
                Triple(8L, 103L, libraryEntryOnly),
            )
            for ((id, storageId, localFileStatus) in duplicateRows) {
                val storageIdSql = storageId?.toString() ?: "NULL"
                execSQL(
                    """
                    INSERT INTO compositions (
                        id, storageId, folderId, fileName, duration, size, addedTime, modifiedTime,
                        storageModifyTime, lastScanTime, missingTime, coverModifyTime, localFileStatus, initialSource
                    ) VALUES (
                        $id, $storageIdSql, $folderId, '$fileName', 120, 1024, 0, 0,
                        0, 0, 0, 0, $localFileStatus, 1
                    )
                    """.trimIndent()
                )
            }

            // Non-duplicate control rows: different fileName, NULL folderId.
            execSQL(
                """
                INSERT INTO compositions (
                    id, storageId, folderId, fileName, duration, size, addedTime, modifiedTime,
                    storageModifyTime, lastScanTime, missingTime, coverModifyTime, localFileStatus, initialSource
                ) VALUES (
                    20, 200, $folderId, 'Other Song.mp3', 120, 1024, 0, 0,
                    0, 0, 0, 0, $available, 1
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO compositions (
                    id, storageId, folderId, fileName, duration, size, addedTime, modifiedTime,
                    storageModifyTime, lastScanTime, missingTime, coverModifyTime, localFileStatus, initialSource
                ) VALUES (
                    21, 201, NULL, '$fileName', 120, 1024, 0, 0,
                    0, 0, 0, 0, $available, 1
                )
                """.trimIndent()
            )

            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (1, 1, 1, 0)")
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (2, 5, 1, 1)")
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (3, 8, 1, 2)")

            execSQL("INSERT INTO play_queue (id, audioId, position, shuffledPosition) VALUES (1, 4, 0, 0)")

            execSQL("INSERT INTO genre_entries (genreId, compositionId, position) VALUES (1, 3, 0)")
            execSQL("INSERT INTO genre_entries (genreId, compositionId, position) VALUES (1, 5, 1)")
            execSQL("INSERT INTO genre_entries (genreId, compositionId, position) VALUES (2, 1, 0)")
        }

        val expectedSurvivorId = 3L

        val db = testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            20,
            true,
            MIGRATION_19_20,
        )

        db.query(
            "SELECT COUNT(*) FROM compositions WHERE folderId = ? AND fileName = ?",
            arrayOf(folderId.toString(), fileName),
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }

        db.query(
            "SELECT id FROM compositions WHERE folderId = ? AND fileName = ?",
            arrayOf(folderId.toString(), fileName),
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(expectedSurvivorId, cursor.getLong(0))
        }

        db.query("SELECT COUNT(*) FROM compositions WHERE id IN (1, 2, 4, 5, 6, 7, 8)").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }

        db.query("SELECT COUNT(*) FROM compositions WHERE id IN (20, 21)").use { cursor ->
            cursor.moveToFirst()
            assertEquals(2, cursor.getInt(0))
        }

        db.query("SELECT audioId FROM play_lists_entries ORDER BY itemId").use { cursor ->
            assertEquals(3, cursor.count)
            cursor.moveToFirst()
            assertEquals(expectedSurvivorId, cursor.getLong(0))
            cursor.moveToNext()
            assertEquals(expectedSurvivorId, cursor.getLong(0))
            cursor.moveToNext()
            assertEquals(expectedSurvivorId, cursor.getLong(0))
        }

        db.query("SELECT audioId FROM play_queue WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals(expectedSurvivorId, cursor.getLong(0))
        }

        db.query(
            "SELECT genreId, compositionId FROM genre_entries ORDER BY genreId, position",
        ).use { cursor ->
            assertEquals(2, cursor.count)

            cursor.moveToFirst()
            assertEquals(1L, cursor.getLong(0))
            assertEquals(expectedSurvivorId, cursor.getLong(1))

            cursor.moveToNext()
            assertEquals(2L, cursor.getLong(0))
            assertEquals(expectedSurvivorId, cursor.getLong(1))
        }

        db.query(
            """
            SELECT COUNT(*) FROM play_lists_entries
            WHERE audioId NOT IN (SELECT id FROM compositions)
            """.trimIndent(),
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }

        db.query(
            """
            SELECT COUNT(*) FROM play_queue
            WHERE audioId NOT IN (SELECT id FROM compositions)
            """.trimIndent(),
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }

        db.query(
            """
            SELECT COUNT(*) FROM genre_entries
            WHERE compositionId NOT IN (SELECT id FROM compositions)
            """.trimIndent(),
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }

        db.close()
    }

    @Test
    fun testMigrationFrom18To19() {
        // Use the production canonical-path convention: no leading slash. The migration
        // now synthesizes the FileVolume from the parentPath via FileVolume.fromCanonicalPath.
        val fakeVolumePath = "storage/emulated/0"
        // Synthesized volume has storageKey == path.
        val expectedVolumeStorageKey = fakeVolumePath
        val fullMusicPath = "$fakeVolumePath/Music"
        val fullRecordingsPath = "$fakeVolumePath/Recordings"

        val fakeStorageFiles = hashMapOf(
            AudioFileKey(fullMusicPath, "Awesome Song.mp3") to createFakeStorageFile(
                storageId = 1L,
                parentPath = fullMusicPath,
                fileName = "Awesome Song.mp3",
            ),
            AudioFileKey(fullRecordingsPath, "song.mp3") to createFakeStorageFile(
                storageId = 2L,
                parentPath = fullRecordingsPath,
                fileName = "song.mp3",
            )
        )

        testHelper.createDatabase(TEST_DB_NAME, 18).apply {
            execSQL("INSERT INTO folders (id, parentId, name) VALUES (10, NULL, 'Music')")
            execSQL("INSERT INTO folders (id, parentId, name) VALUES (11, NULL, 'Recordings')")
            execSQL("""
            INSERT INTO compositions ( 
                id, storageId, folderId, fileName, duration, size, addedTime, modifiedTime, 
                storageModifyTime, lastScanTime, missingTime, coverModifyTime, localFileStatus, initialSource 
            ) VALUES ( 
                1, 1, 10, 'Awesome Song.mp3', 120, 1024, 0, 0, 
                0, 0, 0, 0, 'AVAILABLE', 1 
            )
        """.trimIndent())
            execSQL("""
            INSERT INTO compositions ( 
                id, storageId, folderId, fileName, duration, size, addedTime, modifiedTime, 
                storageModifyTime, lastScanTime, missingTime, coverModifyTime, localFileStatus, initialSource 
            ) VALUES ( 
                2, 2, 11, 'song.mp3', 120, 1024, 0, 0, 
                0, 0, 0, 0, 'AVAILABLE', 1 
            )
        """.trimIndent())

            execSQL("INSERT INTO play_lists (id, name, addedTime, modifiedTime) VALUES (1, 'Test Playlist', 0, 0)")
            // Seed sparse playlist positions: 1, 15, 5
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (1, 1, 1, 1)")
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (3, 1, 1, 15)")
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (2, 2, 1, 5)")

            execSQL("INSERT INTO play_lists (id, name, addedTime, modifiedTime) VALUES (2, 'Descending Playlist', 0, 0)")
            // 2 items: 20 -> 5
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (4, 1, 2, 20)")
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (5, 2, 2, 5)")

            execSQL("INSERT INTO play_lists (id, name, addedTime, modifiedTime) VALUES (3, 'Tied Playlist', 0, 0)")
            // 2 items: 10, 10
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (6, 1, 3, 10)")
            execSQL("INSERT INTO play_lists_entries (itemId, audioId, playListId, orderPosition) VALUES (7, 2, 3, 10)")
        }

        val configTestHelper = MigrationTestHelper(instrumentation, ConfigsDatabase::class.java)
        val configsDb = configTestHelper.createDatabase("configs_database", 2).apply {
            execSQL("INSERT INTO ignored_folders (path, addTime) VALUES ('Recordings', 0)")
            execSQL("INSERT INTO ignored_folders (path, addTime) VALUES ('0/Recordings', 0)")
        }

        val playlistDir = File(context.filesDir, "playlists")
        playlistDir.mkdirs()
        val m3uEditor = M3UEditor()
        val playlistName = "Test Playlist"
        val m3uFile = File(playlistDir, PlaylistFileNameValidator.getPlaylistFileName(playlistName))
        val initialPlaylist = PlayListFile(
            playlistName,
            1000L,
            2000L,
            listOf(
                PlayListEntry("/Music/Awesome Song.mp3"),
                PlayListEntry("/Recordings/song.mp3"),
                PlayListEntry("/Unknown/missing.mp3")
            )
        )
        m3uFile.outputStream().use { stream -> m3uEditor.write(initialPlaylist, stream) }

        try {
            val audioCatalogProvider = mock<SystemAudioCatalogProvider> {
                on { getAudioFiles(eq(0L), eq(true), eq(Constants.DEFAULT_REMOTE_EXTENSIONS)) } doReturn fakeStorageFiles
            }

            val db = testHelper.runMigrationsAndValidate(
                TEST_DB_NAME,
                19,
                true,
                Migrations.getMigration18_19(context, audioCatalogProvider)
            )

            // Assert Volume was created
            db.query("SELECT * FROM volumes").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.count)
                assertEquals(expectedVolumeStorageKey, cursor.getString(cursor.getColumnIndexOrThrow("storageKey")))
                assertEquals(fakeVolumePath, cursor.getString(cursor.getColumnIndexOrThrow("path")))
            }

            // Assert new root folder was created and get its ID
            var newRootFolderId = -1L
            db.query("SELECT id FROM folders WHERE parentId IS NULL AND name = ?", arrayOf(expectedVolumeStorageKey)).use { cursor ->
                assertTrue(cursor.moveToFirst())
                newRootFolderId = cursor.getLong(0)
            }
            assertTrue(newRootFolderId != -1L)

            // Assert old 'Music' folder was re-parented correctly
            db.query("SELECT parentId, volumeId FROM folders WHERE id = 10").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(newRootFolderId, cursor.getLong(cursor.getColumnIndexOrThrow("parentId")))
                assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("volumeId")))
            }

            // Assert old 'Recordings' folder was re-parented correctly
            db.query("SELECT parentId, volumeId FROM folders WHERE id = 11").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(newRootFolderId, cursor.getLong(cursor.getColumnIndexOrThrow("parentId")))
                assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("volumeId")))
            }

            // Assert playlist normalization
            db.query("SELECT itemId, orderPosition FROM play_lists_entries WHERE playListId = 1 ORDER BY orderPosition").use { cursor ->
                assertEquals(3, cursor.count)

                assertTrue(cursor.moveToFirst())
                assertEquals(1L, cursor.getLong(0))
                assertEquals(0, cursor.getInt(1))

                assertTrue(cursor.moveToNext())
                assertEquals(2L, cursor.getLong(0))
                assertEquals(1, cursor.getInt(1))

                assertTrue(cursor.moveToNext())
                assertEquals(3L, cursor.getLong(0))
                assertEquals(2, cursor.getInt(1))
            }

            // Assert descending playlist normalization
            db.query("SELECT itemId, orderPosition FROM play_lists_entries WHERE playListId = 2 ORDER BY orderPosition").use { cursor ->
                assertEquals(2, cursor.count)

                assertTrue(cursor.moveToFirst())
                assertEquals(5L, cursor.getLong(0)) // item with position 5 comes first
                assertEquals(0, cursor.getInt(1))

                assertTrue(cursor.moveToNext())
                assertEquals(4L, cursor.getLong(0)) // item with position 20 comes second
                assertEquals(1, cursor.getInt(1))
            }

            // Assert tied playlist normalization (should tie-break by itemId)
            db.query("SELECT itemId, orderPosition FROM play_lists_entries WHERE playListId = 3 ORDER BY orderPosition").use { cursor ->
                assertEquals(2, cursor.count)

                assertTrue(cursor.moveToFirst())
                assertEquals(6L, cursor.getLong(0))
                assertEquals(0, cursor.getInt(1))

                assertTrue(cursor.moveToNext())
                assertEquals(7L, cursor.getLong(0))
                assertEquals(1, cursor.getInt(1))
            }

            db.close()

            // Assert ignored folders path was updated to the full path
            configsDb.query("SELECT path FROM ignored_folders").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(fullRecordingsPath, cursor.getString(0))
            }
            configsDb.close()

            // Assert playlist cache migration
            val migratedFile = m3uFile.inputStream().use { stream -> m3uEditor.read(playlistName, stream) }
            assertEquals(3, migratedFile.entries.size)
            assertEquals("$fullMusicPath/Awesome Song.mp3", migratedFile.entries[0].filePath)
            assertEquals("$fullRecordingsPath/song.mp3", migratedFile.entries[1].filePath)
            assertEquals("/Unknown/missing.mp3", migratedFile.entries[2].filePath)
            assertEquals(1000L, migratedFile.createDate)
            assertEquals(2000L, migratedFile.modifyDate)
        } finally {
            playlistDir.deleteRecursively()
        }
    }

    @Test
    fun testConfigMigrationFrom1To2() {
        val configTestHelper = MigrationTestHelper(instrumentation, ConfigsDatabase::class.java)
        configTestHelper.createDatabase(TEST_DB_NAME, 1)
        configTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            2,
            false,
            MIGRATION_CONFIG_1_2
        )
    }

    @Test
    fun testMigrationFrom17To18() {
        val db = testHelper.createDatabase(TEST_DB_NAME, 17)
        val corruptionType = CorruptionType.UNSUPPORTED
        val cv = ContentValues().apply {
            put("storageId", 1L)
            put("title", "titleHH")
            put("fileName", "filename34.mp3")
            put("duration", 13)
            put("size", 100)
            put("dateAdded", 0L)
            put("dateModified", 0L)
            put("lastScanDate", 0L)
            put("coverModifyTime", 0L)
            put("initialSource", 1)
            put("corruptionType", corruptionType.name)
        }
        val id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            18,
            false,
            MIGRATION_17_18
        )

        db.query("SELECT corruptionType FROM compositions WHERE id = $id").use { c ->
            c.moveToFirst()
            assertEquals(corruptionType.id, c.getInt(c.getColumnIndexOrThrow("corruptionType")))
        }
    }

    @Test
    fun testMigrationFrom16To17() {
        testHelper.createDatabase(TEST_DB_NAME, 16)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            17,
            false,
            MIGRATION_16_17
        )
    }

    @Test
    fun testMigrationFrom15To16() {
        testHelper.createDatabase(TEST_DB_NAME, 15)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            16,
            false,
            getMigration15_16(context)
        )
    }

    @Test
    fun testMigrationFrom14To15() {
        val db = testHelper.createDatabase(TEST_DB_NAME, 14)
        val longName =
            "আমার-সোনার-বাংলা-আমি-তোমায়-ভালোবাসি-চিরদিন-তোমার-আকাশ-তোমার-বাতাস-আমার-প্রাণে-বাজায়-বাঁশি-ও-মা-ফাগুনে-তোর"
        val cutLongName = PlaylistFileNameValidator.getFormattedPlaylistName(longName)
        val cv = ContentValues()
        cv.put("name", longName)
        cv.put("dateAdded", 0L)
        cv.put("dateModified", 0L)
        db.insert("play_lists", SQLiteDatabase.CONFLICT_ABORT, cv)
        cv.put("name", cutLongName)
        db.insert("play_lists", SQLiteDatabase.CONFLICT_ABORT, cv)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            15,
            false,
            MIGRATION_14_15
        )
    }

    @Test
    fun testMigrationFrom13To14() {
        testHelper.createDatabase(TEST_DB_NAME, 13)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            14,
            false,
            getMigration13_14(context)
        )
    }

    @Test
    fun testMigrationFrom12To13() {
        testHelper.createDatabase(TEST_DB_NAME, 12)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            13,
            false,
            MIGRATION_12_13
        )
    }

    @Test
    fun testMigrationFrom11To12() {
        testHelper.createDatabase(TEST_DB_NAME, 11)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            12,
            false,
            MIGRATION_11_12
        )
    }

    @Test
    fun testMigrationFrom10To11() {
        testHelper.createDatabase(TEST_DB_NAME, 10)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            11,
            false,
            MIGRATION_10_11
        )
    }

    @Test
    fun testMigrationFrom9To10() {
        testHelper.createDatabase(TEST_DB_NAME, 9)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            10,
            false,
            MIGRATION_9_10
        )
    }

    @Test
    fun testMigrationFrom8To9() {
        testHelper.createDatabase(TEST_DB_NAME, 8)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            9,
            false,
            MIGRATION_8_9
        )
    }

    @Test
    fun testMigrationFrom7To8() {
        testHelper.createDatabase(TEST_DB_NAME, 7)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            8,
            false,
            MIGRATION_7_8
        )
    }

    @Test
    fun testMigrationFrom6To7() {
        testHelper.createDatabase(TEST_DB_NAME, 6)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            7,
            false,
            MIGRATION_6_7
        )
    }

    @Test
    fun testMigrationFrom5To6() {
        val db = testHelper.createDatabase(TEST_DB_NAME, 5)
        val cv = ContentValues().apply {
            put("storageId", 1L)
            put("title", "titleHH")
            put("filePath", "test/music/filename34.mp3")
            put("duration", 13)
            put("size", 100)
            put("dateAdded", 0L)
            put("dateModified", 0L)
        }
        val id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            6,
            false,
            MIGRATION_5_6
        )
        db.query("SELECT fileName FROM compositions WHERE id = $id").use { c ->
            c.moveToFirst()
            assertEquals("filename34.mp3", c.getString(c.getColumnIndexOrThrow("fileName")))
        }
    }

    @Test
    fun testMigrationFrom4To5() {
        val db = testHelper.createDatabase(TEST_DB_NAME, 4)
        val cv = ContentValues().apply {
            put("storageId", 1L)
            put("artistId", null as Long?)
            put("title", "titleHH")
            put("filePath", "filePath")
            put("duration", 13)
            put("size", 100)
            put("dateAdded", 0L)
            put("dateModified", 0L)
        }
        val id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv)
        val cvQueueItem = ContentValues().apply {
            put("audioId", id)
            put("position", 0)
            put("shuffledPosition", 0)
        }
        val queueId = db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            5,
            false,
            MIGRATION_4_5
        )
        db.query("SELECT title FROM compositions WHERE id = $id").use { c ->
            c.moveToFirst()
            assertEquals("titleHH", c.getString(c.getColumnIndexOrThrow("title")))
        }
        db.query("SELECT audioId FROM play_queue WHERE id = $queueId").use { cursorQueue ->
            cursorQueue.moveToFirst()
            assertEquals(id, cursorQueue.getLong(cursorQueue.getColumnIndexOrThrow("audioId")))
        }
    }

    @Test
    fun testMigrationFrom3To4() {
        val db = testHelper.createDatabase(TEST_DB_NAME, 3)
        val cv = ContentValues().apply {
            put("storageId", 1L)
            put("artist", "artist")
            put("title", "title")
            put("album", "album")
            put("filePath", "filePath")
            put("duration", "duration")
            put("size", "size")
            put("dateAdded", 0L)
            put("dateModified", 0L)
        }
        val id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            4,
            false,
            getMigration3_4()
        )
        db.query("SELECT name FROM artists WHERE id = (SELECT artistId FROM compositions WHERE id = $id)")
            .use { c ->
                c.moveToFirst()
                assertEquals("artist", c.getString(c.getColumnIndexOrThrow("name")))
            }
    }

    @Test
    fun testMigrationFrom2To3() {
        val db = testHelper.createDatabase(TEST_DB_NAME, 2)

        //add duplicate indexes for test
        val cv = ContentValues().apply {
            put("storageId", 1L)
            put("artist", "artist")
            put("title", "title")
            put("album", "album")
            put("filePath", "filePath")
            put("duration", "duration")
            put("size", "size")
            put("dateAdded", 0L)
            put("dateModified", 0L)
        }
        val id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv)
        val cvQueueItem = ContentValues().apply {
            put("audioId", id)
            put("position", 0)
            put("shuffledPosition", 0)
        }
        db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem)
        db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            3,
            false,
            MIGRATION_2_3
        )
    }

    @Test
    fun testMigrationFrom1To2() {
        testHelper.createDatabase(TEST_DB_NAME, 1)
        testHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            2,
            false,
            getMigration1_2(context)
        )
    }

    companion object {
        private const val TEST_DB_NAME = "music_player_database"
    }

}
