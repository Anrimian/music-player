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

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStorageComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStorageFullComposition;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageCompositionAnalyzerTest {

    private CompositionsDaoWrapper compositionsDao = mock(CompositionsDaoWrapper.class);
    private FoldersDaoWrapper foldersDaoWrapper = mock(FoldersDaoWrapper.class);

    private StorageCompositionAnalyzer analyzer;
    
    @Before
    public void setUp() {
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(new LongSparseArray<>());

        analyzer = new StorageCompositionAnalyzer(compositionsDao, foldersDaoWrapper);
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
        String[] excludedFolders = {"music/wazap"};

        when(foldersDaoWrapper.getIgnoredFolders()).thenReturn(excludedFolders);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, fakeStorageFullComposition(1, "0/etc/sdcard/music/wazap/music-1"));
        newCompositions.put(2, fakeStorageFullComposition(1, "0/etc/sdcard/music/wazap/music-2"));
        newCompositions.put(3, fakeStorageFullComposition(1, "0/etc/sdcard/music/wazap/music-3"));

        analyzer.applyCompositionsData(newCompositions);

        verify(compositionsDao).applyChanges(eq(emptyList()), eq(emptyList()), eq(emptyList()));
    }

}