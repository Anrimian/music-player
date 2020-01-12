package com.github.anrimian.musicplayer.data.database;

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MigrationsTest {

    private static final String TEST_DB_NAME = "music_player_database";

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);

    private Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private Context context = instrumentation.getContext();

    @Rule
    public MigrationTestHelper testHelper =
            new MigrationTestHelper(
                    instrumentation,
                    AppDatabase.class.getCanonicalName(),
                    new FrameworkSQLiteOpenHelperFactory());

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