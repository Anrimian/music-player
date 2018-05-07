package com.github.anrimian.simplemusicplayer.data.storage;

import android.Manifest;
import android.os.Environment;
import android.support.test.rule.GrantPermissionRule;

import com.github.anrimian.simplemusicplayer.data.utils.file.EventType;
import com.github.anrimian.simplemusicplayer.data.utils.file.FileObserverEvent;
import com.github.anrimian.simplemusicplayer.data.utils.file.TestFileObserver;
import com.github.anrimian.simplemusicplayer.data.utils.folders.RecursiveFileObserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import io.reactivex.observers.TestObserver;

public class TestFileObserverTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private File testDirectory;
    private TestFileObserver testFileObserver;

    private TestObserver<FileObserverEvent> fileTestObserver;

    private RecursiveFileObserver observer;

    @Before
    public void setUp() throws Exception {
        File root = Environment.getExternalStorageDirectory();
        testDirectory = new File(root, "/test_directory/");
        testDirectory.mkdirs();
        testFileObserver = new TestFileObserver(testDirectory.getPath());
        fileTestObserver = testFileObserver.getEventObservable().test();
    }

    @Test
    public void createAndDeleteFileTest() throws Exception {
        File file = new File(testDirectory, "test_file");
        fileTestObserver.assertValue(event -> event.getAction() == EventType.CREATE);
        file.delete();
        fileTestObserver.assertValue(event -> event.getAction() == EventType.DELETE);
    }

    @Test
    public void recursiveObserverTest() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        testDirectory.delete();
    }
}