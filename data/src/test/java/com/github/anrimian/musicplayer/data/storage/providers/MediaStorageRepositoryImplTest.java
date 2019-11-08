package com.github.anrimian.musicplayer.data.storage.providers;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.playlist.RawPlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStorageComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeStorageCompositionsMap;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayList;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayLists;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayListsAsList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediaStorageRepositoryImplTest {

    private StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);
    private StoragePlayListsProvider playListsProvider = mock(StoragePlayListsProvider.class);
    private CompositionsDaoWrapper compositionsDao = mock(CompositionsDaoWrapper.class);
    private PlayListsDaoWrapper playListsDao = mock(PlayListsDaoWrapper.class);

    private PublishSubject<LongSparseArray<StorageComposition>> newCompositionsSubject = PublishSubject.create();
    private PublishSubject<LongSparseArray<StoragePlayList>> newPlayListsSubject = PublishSubject.create();
    private PublishSubject<List<StoragePlayListItem>> newPlayListItemsSubject = PublishSubject.create();

    private MediaStorageRepositoryImpl mediaStorageRepository;

    @Before
    public void setUp() {
        when(musicProvider.getCompositionsObservable()).thenReturn(newCompositionsSubject);
        when(musicProvider.getCompositions()).thenReturn(new LongSparseArray<>());

        when(playListsProvider.getPlayLists()).thenReturn(storagePlayLists(10));
        when(playListsProvider.getPlayListsObservable()).thenReturn(newPlayListsSubject);
        when(playListsProvider.getPlayListEntriesObservable(1L)).thenReturn(newPlayListItemsSubject);

        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(new LongSparseArray<>());

        when(playListsDao.getPlayListsIds()).thenReturn(asList(new IdPair(1L, 1L)));

        mediaStorageRepository = new MediaStorageRepositoryImpl(musicProvider,
                playListsProvider,
                compositionsDao,
                playListsDao,
                Schedulers.trampoline());
        mediaStorageRepository.initialize();
    }

    @Test
    public void changeDatabaseTest() {
        LongSparseArray<StorageComposition> currentCompositions = getFakeStorageCompositionsMap();
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        LongSparseArray<StorageComposition> newCompositions = getFakeStorageCompositionsMap();
        StorageComposition removedComposition = newCompositions.get(100L);
        newCompositions.remove(100L);

        StorageComposition changedComposition = fakeStorageComposition(1, "changed composition", 1L, 10000L);
        newCompositions.put(1L, changedComposition);

        StorageComposition newComposition = fakeStorageComposition(-1L, "new composition");
        newCompositions.put(-1L, newComposition);

        newCompositionsSubject.onNext(newCompositions);

        verify(compositionsDao).applyChanges(
                eq(asList(newComposition)),
                eq(asList(removedComposition)),
                eq(asList(changedComposition))
        );
    }

    @Test
    public void testUpdateTimeChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        map.put(1L, fakeStorageComposition(1L, "test", 1, 1000));

        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageComposition> newCompositions = new LongSparseArray<>();
        StorageComposition changedComposition = fakeStorageComposition(1, "new path", 1, 1000);
        newCompositions.put(1L, changedComposition);

        newCompositionsSubject.onNext(newCompositions);

        verify(compositionsDao, never()).applyChanges(
                eq(Collections.emptyList()),
                eq(Collections.emptyList()),
                eq(asList(changedComposition))
        );
    }

    @Test
    public void changePlayListTest() {
        when(playListsDao.getAllAsStoragePlayLists()).thenReturn(storagePlayListsAsList(10));

        LongSparseArray<StoragePlayList> newPlayLists = storagePlayLists(10);

        StoragePlayList changedPlayList = new StoragePlayList(1,
                "changed play list",
                new Date(1L),
                new Date(10000L)
        );
        newPlayLists.put(1L, changedPlayList);

        StoragePlayList newPlayList = new StoragePlayList(-1L,
                "new play list",
                new Date(1L),
                new Date(1L)
        );
        newPlayLists.put(-1L, newPlayList);

        newPlayListsSubject.onNext(newPlayLists);

        verify(playListsDao).applyChanges(
                eq(asList(newPlayList)),
                eq(asList(changedPlayList))
        );
    }

    @Test
    public void changePlayListItemsTest() {
        when(playListsDao.getAllAsStoragePlayLists()).thenReturn(asList(storagePlayList(1L)));
        when(playListsDao.getPlayListItemsAsStorageItems(1L)).thenReturn(Collections.emptyList());

        List<StoragePlayListItem> newItems = asList(
                new StoragePlayListItem(1L, 1L)
        );

        newPlayListItemsSubject.onNext(newItems);

        verify(playListsDao).insertPlayListItems(
                eq(asList(new RawPlayListItem(1L, 0))),
                eq(1L)
        );
    }
}