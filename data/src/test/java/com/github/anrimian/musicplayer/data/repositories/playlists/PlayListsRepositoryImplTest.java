package com.github.anrimian.musicplayer.data.repositories.playlists;

import com.github.anrimian.musicplayer.data.models.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayList;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayListRepositoryImplTest {

    private StoragePlayListsProvider storagePlayListsProvider = mock(StoragePlayListsProvider.class);

    private PublishSubject<List<StoragePlayList>> playListSubject = PublishSubject.create();
    private PublishSubject<List<Composition>> compositionSubject = PublishSubject.create();

    private PlayListRepositoryImpl playListRepositoryImpl = new PlayListRepositoryImpl(
            storagePlayListsProvider, Schedulers.trampoline());

    @Before
    public void setUp() {
        when(storagePlayListsProvider.getPlayLists()).thenReturn(ListUtils.asList(storagePlayList(1L)));
        when(storagePlayListsProvider.getCompositions(anyLong())).thenReturn(getFakeCompositions());
        when(storagePlayListsProvider.getChangeObservable()).thenReturn(playListSubject);
        when(storagePlayListsProvider.getPlayListChangeObservable(anyLong())).thenReturn(compositionSubject);
    }

    @Test
    public void testPlayListsReceiving() {
        TestObserver<List<PlayList>> testObserver = playListRepositoryImpl
                .getPlayListsObservable()
                .test();

        testObserver.assertValueAt(0, list -> {
            PlayList playList = list.get(0);
            assertEquals(1L, playList.getId());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        playListSubject.onNext(ListUtils.asList(storagePlayList(2L)));

        testObserver.assertValueAt(1, list -> {
            PlayList playList = list.get(0);
            assertEquals(2L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        testObserver.dispose();

        compositionSubject.onNext(ListUtils.asList(fakeComposition(0)));

        testObserver = playListRepositoryImpl
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
        TestObserver<List<PlayList>> testObserver = playListRepositoryImpl
                .getPlayListsObservable()
                .test();

        List<StoragePlayList> newList = asList(storagePlayList(1L), storagePlayList(2L));

        playListSubject.onNext(newList);

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

        playListRepositoryImpl
                .getPlayListsObservable()
                .test()
                .assertValue(updatedListPredicate);
    }

    @Test
    public void getCompositionsObservableTest() {
        TestObserver<List<Composition>> testObserver = playListRepositoryImpl
                .getCompositionsObservable(1L)
                .test();

        testObserver.assertValue(getFakeCompositions());

        compositionSubject.onNext(ListUtils.asList(fakeComposition(0)));

        testObserver.assertValueAt(1, ListUtils.asList(fakeComposition(0)));
    }
}