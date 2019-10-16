package com.github.anrimian.musicplayer.data.repositories.playlists;

import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakePlayListItem;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStoragePlayListItem;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositionsMap;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakePlayListItems;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeStoragePlayListItems;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayListsRepositoryImplTest {

    private StoragePlayListsProvider storagePlayListsProvider = mock(StoragePlayListsProvider.class);
    private StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private PlayListsDaoWrapper playListsDaoWrapper = mock(PlayListsDaoWrapper.class);

    private PublishSubject<Map<Long, StoragePlayList>> playListSubject = PublishSubject.create();
    private PublishSubject<List<StoragePlayListItem>> itemsSubject = PublishSubject.create();
    private PublishSubject<Map<Long, Composition>> compositionsSubject = PublishSubject.create();

    private PlayListsRepositoryImpl playListsRepositoryImpl = new PlayListsRepositoryImpl(
            storagePlayListsProvider,
            storageMusicDataSource,
            playListsDaoWrapper,
            Schedulers.trampoline());

    @Before
    public void setUp() {
        when(storagePlayListsProvider.getPlayLists()).thenReturn(Collections.singletonMap(1L, storagePlayList(1L)));
        when(storagePlayListsProvider.getPlayListItems(anyLong())).thenReturn(getFakeStoragePlayListItems());
        when(storagePlayListsProvider.getPlayListsObservable()).thenReturn(playListSubject);
        when(storagePlayListsProvider.getPlayListEntriesObservable(anyLong())).thenReturn(itemsSubject);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getFakeCompositionsMap());
        when(storageMusicDataSource.getCompositionObservable()).thenReturn(compositionsSubject);
    }

    @Test
    public void testPlayListsReceiving() {
        TestObserver<List<PlayList>> testObserver = playListsRepositoryImpl
                .getPlayListsObservable()
                .test();

        testObserver.assertValueAt(0, list -> {
            PlayList playList = list.get(0);
            assertEquals(1L, playList.getId());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        playListSubject.onNext(Collections.singletonMap(2L, storagePlayList(2L)));

        testObserver.assertValueAt(1, list -> {
            PlayList playList = list.get(0);
            assertEquals(2L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        testObserver.dispose();

        TestObserver<PlayList> playListTestObserver = playListsRepositoryImpl
                .getPlayListObservable(2L)
                .test();

        playListTestObserver.assertValueAt(0, playList -> {
            assertEquals(2L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        itemsSubject.onNext(ListUtils.asList(fakeStoragePlayListItem(0)));

        playListTestObserver.assertValueAt(1, playList -> {//error: not received
            assertEquals(2L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(1, playList.getCompositionsCount());
            return true;
        });

        testObserver = playListsRepositoryImpl
                .getPlayListsObservable()
                .test();

        testObserver.assertValueAt(0, list -> {
            PlayList playList = list.get(0);
            assertEquals(2L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(1, playList.getCompositionsCount());
            return true;
        });
    }

    @Test
    public void testNewPlayListsAdded() {
        TestObserver<List<PlayList>> testObserver = playListsRepositoryImpl
                .getPlayListsObservable()
                .test();

        Map<Long, StoragePlayList> newMap = new HashMap<>();
        newMap.put(1L, storagePlayList(1L));
        newMap.put(2L, storagePlayList(2L));

        playListSubject.onNext(newMap);

        Predicate<List<PlayList>> updatedListPredicate = list -> {
            assertEquals(2, list.size());

            PlayList playListOne = list.get(0);
            assertEquals(2L, playListOne.getId());
            assertEquals("test2", playListOne.getName());

            PlayList playListTwo = list.get(1);
            assertEquals(1L, playListTwo.getId());
            assertEquals("test1", playListTwo.getName());
            return true;
        };

        testObserver.assertValueAt(1, updatedListPredicate);

        playListsRepositoryImpl
                .getPlayListsObservable()
                .test()
                .assertValue(updatedListPredicate);
    }

    @Test
    public void getCompositionsObservableTest() {
        TestObserver<List<PlayListItem>> testObserver = playListsRepositoryImpl
                .getCompositionsObservable(1L)
                .test();

        testObserver.assertValue(getFakePlayListItems());

        itemsSubject.onNext(ListUtils.asList(fakeStoragePlayListItem(0)));

        testObserver.assertValueAt(1, ListUtils.asList(fakePlayListItem(0)));
    }
}