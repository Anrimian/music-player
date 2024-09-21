package com.github.anrimian.musicplayer.data.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlayListFileNameValidator;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MigrationsTest {

    private static final String TEST_DB_NAME = "music_player_database";

    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context context = instrumentation.getContext();

    public MigrationTestHelper testHelper = new MigrationTestHelper(
                    instrumentation,
                    LibraryDatabase.class.getCanonicalName()
    );

    @Test
    public void testMigrationFrom15To16() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 15);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                16,
                false,
                Migrations.getMigration15_16(context));
    }

    @Test
    public void testMigrationFrom14To15() throws Exception {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 14);
        String longName = "আমার-সোনার-বাংলা-আমি-তোমায়-ভালোবাসি-চিরদিন-তোমার-আকাশ-তোমার-বাতাস-আমার-প্রাণে-বাজায়-বাঁশি-ও-মা-ফাগুনে-তোর";
        String cutLongName = PlayListFileNameValidator.INSTANCE.getFormattedPlaylistName(longName);
        ContentValues cv = new ContentValues();
        cv.put("name", longName);
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        db.insert("play_lists", SQLiteDatabase.CONFLICT_ABORT, cv);
        cv.put("name", cutLongName);
        db.insert("play_lists", SQLiteDatabase.CONFLICT_ABORT, cv);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                15,
                false,
                Migrations.MIGRATION_14_15);
    }

    @Test
    public void testMigrationFrom13To14() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 13);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                14,
                false,
                Migrations.getMigration13_14(context));
    }

    @Test
    public void testMigrationFrom12To13() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 12);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                13,
                false,
                Migrations.MIGRATION_12_13);
    }

    @Test
    public void testMigrationFrom11To12() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 11);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                12,
                false,
                Migrations.MIGRATION_11_12);
    }

    @Test
    public void testMigrationFrom10To11() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 10);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                11,
                false,
                Migrations.MIGRATION_10_11);
    }

    @Test
    public void testMigrationFrom9To10() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 9);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                10,
                false,
                Migrations.MIGRATION_9_10);
    }

    @Test
    public void testMigrationFrom8To9() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 8);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                9,
                false,
                Migrations.MIGRATION_8_9);
    }

    @Test
    public void testMigrationFrom7To8() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 7);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                8,
                false,
                Migrations.MIGRATION_7_8);
    }

    @Test
    public void testMigrationFrom6To7() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 6);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                7,
                false,
                Migrations.MIGRATION_6_7);
    }

    @Test
    public void testMigrationFrom5To6() throws IOException {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 5);

        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("title", "titleHH");
        cv.put("filePath", "test/music/filename34.mp3");
        cv.put("duration", 13);
        cv.put("size", 100);
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                6,
                false,
                Migrations.MIGRATION_5_6);

        Cursor c = db.query("SELECT fileName FROM compositions WHERE id = " + id);
        c.moveToFirst();
        assertEquals("filename34.mp3", c.getString(c.getColumnIndex("fileName")));
    }

    @Test
    public void testMigrationFrom4To5() throws IOException {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 4);

        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("artistId", (Long) null);
        cv.put("title", "titleHH");
        cv.put("filePath", "filePath");
        cv.put("duration", 13);
        cv.put("size", 100);
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);

        ContentValues cvQueueItem = new ContentValues();
        cvQueueItem.put("audioId", id);
        cvQueueItem.put("position", 0);
        cvQueueItem.put("shuffledPosition", 0);
        long queueId = db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                5,
                false,
                Migrations.MIGRATION_4_5);

        Cursor c = db.query("SELECT title FROM compositions WHERE id = " + id);
        c.moveToFirst();
        assertEquals("titleHH", c.getString(c.getColumnIndex("title")));

        Cursor cursorQueue = db.query("SELECT audioId FROM play_queue WHERE id = " + queueId);
        cursorQueue.moveToFirst();
        assertEquals(id, cursorQueue.getLong(cursorQueue.getColumnIndex("audioId")));
    }

    @Test
    public void testMigrationFrom3To4() throws IOException {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 3);

        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("artist", "artist");
        cv.put("title", "title");
        cv.put("album", "album");
        cv.put("filePath", "filePath");
        cv.put("duration", "duration");
        cv.put("size", "size");
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                4,
                false,
                Migrations.getMigration3_4(context));

        Cursor c = db.query("SELECT name FROM artists WHERE id = (SELECT artistId FROM compositions WHERE id = " + id + ")");
        c.moveToFirst();
        assertEquals("artist", c.getString(c.getColumnIndex("name")));
    }

    @Test
    public void testMigrationFrom2To3() throws Exception {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 2);

        //add duplicate indexes for test
        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("artist", "artist");
        cv.put("title", "title");
        cv.put("album", "album");
        cv.put("filePath", "filePath");
        cv.put("duration", "duration");
        cv.put("size", "size");
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);
        ContentValues cvQueueItem = new ContentValues();
        cvQueueItem.put("audioId", id);
        cvQueueItem.put("position", 0);
        cvQueueItem.put("shuffledPosition", 0);
        db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem);
        db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                3,
                false,
                Migrations.MIGRATION_2_3);
    }

    @Test
    public void testMigrationFrom1To2() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 1);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                2,
                false,
                Migrations.getMigration1_2(context));
    }
}