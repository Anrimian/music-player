package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStorageComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStorageFullComposition;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageCompositionAnalyzerTest {

    private CompositionsDaoWrapper compositionsDao = mock(CompositionsDaoWrapper.class);
    private FoldersDaoWrapper foldersDao = mock(FoldersDaoWrapper.class);

    private StorageCompositionAnalyzer analyzer;
    
    @Before
    public void setUp() {
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(new LongSparseArray<>());

        when(foldersDao.getIgnoredFolders()).thenReturn(new String[0]);

        analyzer = new StorageCompositionAnalyzer(compositionsDao, foldersDao);
    }

    @Test
    public void changeDatabaseTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        currentCompositions.put(1, fakeStorageComposition(1, "music-1"));
        currentCompositions.put(2, fakeStorageComposition(2, "music-2"));
        currentCompositions.put(3, fakeStorageComposition(3, "music-3"));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, fakeStorageFullComposition(1, "music-1"));
        StorageFullComposition changedComposition = fakeStorageFullComposition(3, "changed composition", 1L, 10000L);
        newCompositions.put(3L, changedComposition);
        newCompositions.put(4, fakeStorageFullComposition(4, "music-4"));

        analyzer.applyCompositionsData(newCompositions);

        verify(compositionsDao).applyChanges(
                eq(asList(fakeStorageFullComposition(4, "music-4"))),//new
                eq(asList(fakeStorageComposition(2, "music-2"))),//removed
                eq(asList(new Change<>(fakeStorageComposition(3, "music-3"), changedComposition)))
        );
    }

    @Test
    public void testUpdateTimeChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        map.put(1L, fakeStorageComposition(1L, "test", 1, 1000));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = fakeStorageFullComposition(1, "new path", 1, 1000);
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(compositionsDao, never()).applyChanges(
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(fakeStorageComposition(1L, "test", 1, 1000), changedComposition)))
        );
    }

    @Test
    public void testUpdateArtistChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        map.put(1L, fakeStorageComposition(1L, "test", 1, 1));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = new StorageFullComposition("new artist",
                null,
                "test",
                "test",
                0,
                0,
                1L,
                new Date(1),
                new Date(1000),
                null);
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(compositionsDao).applyChanges(
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(fakeStorageComposition(1L, "test", 1, 1), changedComposition)))
        );
    }

    @Test
    public void testUpdateAlbumChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        map.put(1L, fakeStorageComposition(1L, "test", 1, 1));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = new StorageFullComposition(null,
                null,
                "test",
                "test",
                0,
                0,
                1L,
                new Date(1),
                new Date(1000),
                new StorageAlbum(1, "test album", null, 0, 1));
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(compositionsDao).applyChanges(
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(fakeStorageComposition(1L, "test", 1, 1), changedComposition)))
        );
    }

    @Test
    public void testUpdateAlbumArtistChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        StorageComposition oldComposition = new StorageComposition(null,
                "album artist",
                null,
                "test album",
                "test",
                0,
                0,
                1L,
                1L,
                new Date(1),
                new Date(1));
        map.put(1L, oldComposition);
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = new StorageFullComposition(null,
                null,
                "test",
                "test",
                0,
                0,
                1L,
                new Date(1),
                new Date(1000),
                new StorageAlbum(1, "test album", "new album artist", 0, 1));
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(compositionsDao).applyChanges(
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(oldComposition, changedComposition)))
        );
    }

    @Test
    public void excludeFoldersTest() {
        String[] excludedFolders = {"music/wazap", "rubbish"};

        when(foldersDao.getIgnoredFolders()).thenReturn(excludedFolders);

        StorageFullComposition expectedComposition = fakeStorageFullComposition(4, "0/etc/sdcard/music-4");

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, fakeStorageFullComposition(1, "0/etc/sdcard/music/wazap/music-1"));
        newCompositions.put(2, fakeStorageFullComposition(2, "0/etc/sdcard/music/wazap/music-2"));
        newCompositions.put(3, fakeStorageFullComposition(3, "0/etc/sdcard/music/wazap/music-3"));
        newCompositions.put(4, expectedComposition);
        newCompositions.put(5, fakeStorageFullComposition(5, "0/etc/sdcard/rubbish"));

        analyzer.applyCompositionsData(newCompositions);

        verify(compositionsDao).applyChanges(eq(asList(expectedComposition)), eq(emptyList()), eq(emptyList()));
    }

    @Test
    public void insertFolderTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        StorageFullComposition c1 = fakeStorageFullComposition(1, "music/music-1", "music");
        StorageFullComposition c2 = fakeStorageFullComposition(2, "music/new/music-2", "music/new");
        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, c1);
        newCompositions.put(2, c2);

        analyzer.applyCompositionsData(newCompositions);

        Set<String> expectedPathsToInsert = new HashSet<>();
        expectedPathsToInsert.add("music");
        expectedPathsToInsert.add("music/new");
        verify(foldersDao).insertFolders(
                eq(expectedPathsToInsert),
                any(),
                any()
        );

        verify(compositionsDao).applyChanges(
                eq(asList(c1, c2)),
                eq(emptyList()),
                eq(emptyList())
        );
    }

}