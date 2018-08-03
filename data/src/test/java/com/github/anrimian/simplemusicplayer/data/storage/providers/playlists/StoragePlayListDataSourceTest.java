package com.github.anrimian.simplemusicplayer.data.storage.providers.playlists;

import com.github.anrimian.simplemusicplayer.data.models.StoragePlayList;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.domain.utils.ListUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositions;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoragePlayListDataSourceTest {

    private StoragePlayListsProvider storagePlayListsProvider = mock(StoragePlayListsProvider.class);

    private PublishSubject<List<StoragePlayList>> playListSubject = PublishSubject.create();
    private PublishSubject<List<Composition>> compositionSubject = PublishSubject.create();

    private StoragePlayListDataSource storagePlayListDataSource = new StoragePlayListDataSource(
            storagePlayListsProvider);

    @Before
    public void setUp() {
        when(storagePlayListsProvider.getPlayLists()).thenReturn(ListUtils.asList(getTestPlayList()));
        when(storagePlayListsProvider.getCompositions(eq(1L))).thenReturn(getFakeCompositions());
        when(storagePlayListsProvider.getChangeObservable()).thenReturn(playListSubject);
        when(storagePlayListsProvider.getPlayListChangeObservable(anyLong())).thenReturn(compositionSubject);
    }

    @Test
    public void testPlayListsReceiving() {
        TestObserver<List<PlayList>> testObserver = storagePlayListDataSource
                .getPlayListsObservable()
                .test();

        testObserver.assertValueAt(0, list -> {
            PlayList playList = list.get(0);
            assertEquals(1L, playList.getId());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        playListSubject.onNext(ListUtils.asList(new StoragePlayList(1L, "test2", new Date(), new Date())));

        testObserver.assertValueAt(1, list -> {
            PlayList playList = list.get(0);
            assertEquals(1L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        compositionSubject.onNext(ListUtils.asList(fakeComposition(0)));

        testObserver.assertValueAt(2, list -> {
            PlayList playList = list.get(0);
            assertEquals(1L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(1, playList.getCompositionsCount());
            return true;
        });
    }

    @Test
    public void testNewPlayListsAdded() {
        TestObserver<List<PlayList>> testObserver = storagePlayListDataSource
                .getPlayListsObservable()
                .test();

        List<StoragePlayList> newList = asList(
                new StoragePlayList(1L, "test2", new Date(), new Date()),
                new StoragePlayList(2L, "test3", new Date(), new Date())
        );

        playListSubject.onNext(newList);

        Predicate<List<PlayList>> updatedListPredicate = list -> {
            assertEquals(2, list.size());

            PlayList playListOne = list.get(0);
            assertEquals(1L, playListOne.getId());
            assertEquals("test2", playListOne.getName());

            PlayList playListTwo = list.get(1);
            assertEquals(2L, playListTwo.getId());
            assertEquals("test3", playListTwo.getName());
            return true;
        };

        testObserver.assertValueAt(1, updatedListPredicate);

        storagePlayListDataSource
                .getPlayListsObservable()
                .test()
                .assertValue(updatedListPredicate);
    }

    @Test
    public void getCompositionsObservableTest() {
        TestObserver<List<Composition>> testObserver = storagePlayListDataSource
                .getCompositionsObservable(1L)
                .test();

        testObserver.assertValue(getFakeCompositions());

        compositionSubject.onNext(ListUtils.asList(fakeComposition(0)));

        testObserver.assertValueAt(1, ListUtils.asList(fakeComposition(0)));
    }

    private StoragePlayList getTestPlayList() {
        return new StoragePlayList(1L, "test", new Date(), new Date());
    }
}