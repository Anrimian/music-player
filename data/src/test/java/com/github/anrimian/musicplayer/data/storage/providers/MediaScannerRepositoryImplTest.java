package com.github.anrimian.musicplayer.data.storage.providers;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.repositories.scanner.MediaScannerRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStorageComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeStorageFullComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayLists;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.storagePlayListsAsList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediaScannerRepositoryImplTest {

    private StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);
    private StoragePlayListsProvider playListsProvider = mock(StoragePlayListsProvider.class);
    private StorageArtistsProvider artistsProvider = mock(StorageArtistsProvider.class);
    private StorageAlbumsProvider albumsProvider = mock(StorageAlbumsProvider.class);
    private StorageGenresProvider genresProvider = mock(StorageGenresProvider.class);
    private CompositionsDaoWrapper compositionsDao = mock(CompositionsDaoWrapper.class);
    private PlayListsDaoWrapper playListsDao = mock(PlayListsDaoWrapper.class);
    private GenresDaoWrapper genresDao = mock(GenresDaoWrapper.class);

    private PublishSubject<LongSparseArray<StorageFullComposition>> newCompositionsSubject = PublishSubject.create();
    private PublishSubject<LongSparseArray<StorageAlbum>> newAlbumsSubject = PublishSubject.create();
    private PublishSubject<Map<String, StorageArtist>> newArtistsSubject = PublishSubject.create();
    private PublishSubject<LongSparseArray<StoragePlayList>> newPlayListsSubject = PublishSubject.create();
    private PublishSubject<List<StoragePlayListItem>> newPlayListItemsSubject = PublishSubject.create();
    private PublishSubject<Map<String, StorageGenre>> newGenreSubject = PublishSubject.create();

    private MediaScannerRepositoryImpl mediaStorageRepository;

    @Before
    public void setUp() {
        when(albumsProvider.getAlbumsObservable()).thenReturn(newAlbumsSubject);
        when(albumsProvider.getAlbums()).thenReturn(new LongSparseArray<>());

        when(genresProvider.getGenresObservable()).thenReturn(newGenreSubject);
        when(genresProvider.getGenres()).thenReturn(new HashMap<>());

        when(artistsProvider.getArtistsObservable()).thenReturn(newArtistsSubject);
        when(artistsProvider.getArtists()).thenReturn(new HashMap<>());

        when(musicProvider.getCompositionsObservable()).thenReturn(newCompositionsSubject);
        when(musicProvider.getCompositions()).thenReturn(new LongSparseArray<>());

        when(playListsProvider.getPlayLists()).thenReturn(storagePlayLists(10));
        when(playListsProvider.getPlayListsObservable()).thenReturn(newPlayListsSubject);
        when(playListsProvider.getPlayListEntriesObservable(1L)).thenReturn(newPlayListItemsSubject);

        when(genresDao.selectAllGenreNames()).thenReturn(new HashSet<>());

        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(new LongSparseArray<>());

        when(playListsDao.getPlayListsIds()).thenReturn(asList(new IdPair(1L, 1L)));

        mediaStorageRepository = new MediaScannerRepositoryImpl(musicProvider,
                playListsProvider,
                genresProvider,
                compositionsDao,
                playListsDao,
                genresDao,
                Schedulers.trampoline());
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
        when(musicProvider.getCompositions()).thenReturn(newCompositions);

        mediaStorageRepository.runStorageObserver();

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
        when(musicProvider.getCompositions()).thenReturn(newCompositions);

        mediaStorageRepository.runStorageObserver();

        verify(compositionsDao, never()).applyChanges(
                eq(Collections.emptyList()),
                eq(Collections.emptyList()),
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
        when(musicProvider.getCompositions()).thenReturn(newCompositions);

        mediaStorageRepository.runStorageObserver();

        verify(compositionsDao).applyChanges(
                eq(Collections.emptyList()),
                eq(Collections.emptyList()),
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
        when(musicProvider.getCompositions()).thenReturn(newCompositions);

        mediaStorageRepository.runStorageObserver();

        verify(compositionsDao).applyChanges(
                eq(Collections.emptyList()),
                eq(Collections.emptyList()),
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
        when(musicProvider.getCompositions()).thenReturn(newCompositions);

        mediaStorageRepository.runStorageObserver();

        verify(compositionsDao).applyChanges(
                eq(Collections.emptyList()),
                eq(Collections.emptyList()),
                eq(asList(new Change<>(oldComposition, changedComposition)))
        );
    }

    @Test
    public void changePlayListTest() {
        when(playListsDao.getAllAsStoragePlayLists()).thenReturn(storagePlayListsAsList(10));

        mediaStorageRepository.runStorageObserver();

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
        when(playListsProvider.getPlayLists()).thenReturn(storagePlayLists(1));
        when(playListsDao.getPlayListItemsAsStorageItems(1L)).thenReturn(Collections.emptyList());
        when(playListsDao.isPlayListExists(1L)).thenReturn(true);

        mediaStorageRepository.runStorageObserver();

        List<StoragePlayListItem> newItems = asList(
                new StoragePlayListItem(1L, 1L)
        );

        newPlayListItemsSubject.onNext(newItems);

        verify(playListsDao).insertPlayListItems(
                eq(asList(new StoragePlayListItem(1L, 0))),
                eq(1L)
        );
    }
}