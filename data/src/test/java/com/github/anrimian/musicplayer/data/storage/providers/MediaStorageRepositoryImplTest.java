package com.github.anrimian.musicplayer.data.storage.providers;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositionsMap;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayLists;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
        when(musicProvider.getCompositions()).thenReturn(getFakeCompositionsMap());
        when(musicProvider.getCompositionsObservable()).thenReturn(newCompositionsSubject);

        when(playListsProvider.getPlayLists()).thenReturn(storagePlayLists(10));
        when(playListsProvider.getPlayListsObservable()).thenReturn(newPlayListsSubject);

        mediaStorageRepository = new MediaStorageRepositoryImpl(musicProvider,
                playListsProvider,
                compositionsDao,
                playListsDao,
                Schedulers.trampoline());
    }

    @Test
    public void changeDatabaseTest() {
        mediaStorageRepository.initialize();

        Map<Long, Composition> currentCompositions = getFakeCompositionsMap();
        when(compositionsDao.getAllMap()).thenReturn(currentCompositions);

        Map<Long, Composition> newCompositions = getFakeCompositionsMap();
        Composition removedComposition = newCompositions.remove(100L);

        Composition changedComposition = fakeComposition(1, "new path");
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
}