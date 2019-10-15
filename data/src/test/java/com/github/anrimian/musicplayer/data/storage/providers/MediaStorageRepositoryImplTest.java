package com.github.anrimian.musicplayer.data.storage.providers;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositionsMap;
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

    private PublishSubject<Map<Long, Composition>> newCompositionsSubject = PublishSubject.create();
    private PublishSubject<Map<Long, StoragePlayList>> newPlayListsSubject = PublishSubject.create();

    private MediaStorageRepositoryImpl mediaStorageRepository;

    @Before
    public void setUp() {
        when(musicProvider.getCompositionsObservable()).thenReturn(newCompositionsSubject);

        when(playListsProvider.getPlayLists()).thenReturn(storagePlayLists(10));
        when(playListsProvider.getPlayListsObservable()).thenReturn(newPlayListsSubject);

        mediaStorageRepository = new MediaStorageRepositoryImpl(musicProvider,
                playListsProvider,
                compositionsDao,
                playListsDao,
                Schedulers.trampoline());
        mediaStorageRepository.initialize();
    }

    @Test
    public void changeDatabaseTest() {
        Map<Long, Composition> currentCompositions = getFakeCompositionsMap();
        when(compositionsDao.getAllMap()).thenReturn(currentCompositions);

        Map<Long, Composition> newCompositions = getFakeCompositionsMap();
        Composition removedComposition = newCompositions.remove(100L);

        Composition changedComposition = fakeComposition(1, "changed composition", 1L, 10000L);
        newCompositions.put(1L, changedComposition);

        Composition newComposition = fakeComposition(-1L, "new composition");
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
        HashMap<Long, Composition> map = new HashMap<>();
        map.put(1L, fakeComposition(1L, "test", 1, 1000));

        when(compositionsDao.getAllMap()).thenReturn(map);

        HashMap<Long, Composition> newCompositions = new HashMap<>();
        Composition changedComposition = fakeComposition(1, "new path", 1, 1000);
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

        Map<Long, StoragePlayList> newPlayLists = storagePlayLists(10);

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
}