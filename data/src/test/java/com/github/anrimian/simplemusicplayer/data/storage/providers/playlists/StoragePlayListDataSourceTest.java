package com.github.anrimian.simplemusicplayer.data.storage.providers.playlists;

import com.github.anrimian.simplemusicplayer.data.models.StoragePlayList;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.simplemusicplayer.domain.utils.ListUtils.asList;
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
        when(storagePlayListsProvider.getPlayLists()).thenReturn(asList(getTestPlayList()));
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

        playListSubject.onNext(asList(new StoragePlayList(1L, "test2", new Date(), new Date())));

        testObserver.assertValueAt(1, list -> {
            PlayList playList = list.get(0);
            assertEquals(1L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(getFakeCompositions().size(), playList.getCompositionsCount());
            return true;
        });

        compositionSubject.onNext(asList(fakeComposition(0)));

        testObserver.assertValueAt(2, list -> {
            PlayList playList = list.get(0);
            assertEquals(1L, playList.getId());
            assertEquals("test2", playList.getName());
            assertEquals(1, playList.getCompositionsCount());
            return true;
        });
    }

    private StoragePlayList getTestPlayList() {
        return new StoragePlayList(1L, "test", new Date(), new Date());
    }
}