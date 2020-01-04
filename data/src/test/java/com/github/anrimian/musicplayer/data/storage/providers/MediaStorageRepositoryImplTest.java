package com.github.anrimian.musicplayer.data.storage.providers;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.albums.ShortAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
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
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeStorageCompositionsMap;
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
    private StorageArtistsProvider artistsProvider = mock(StorageArtistsProvider.class);
    private StorageAlbumsProvider albumsProvider = mock(StorageAlbumsProvider.class);
    private StorageGenresProvider genresProvider = mock(StorageGenresProvider.class);
    private CompositionsDaoWrapper compositionsDao = mock(CompositionsDaoWrapper.class);
    private PlayListsDaoWrapper playListsDao = mock(PlayListsDaoWrapper.class);
    private ArtistsDaoWrapper artistsDao = mock(ArtistsDaoWrapper.class);
    private AlbumsDaoWrapper albumsDao = mock(AlbumsDaoWrapper.class);
    private GenresDaoWrapper genresDao = mock(GenresDaoWrapper.class);

    private PublishSubject<LongSparseArray<StorageComposition>> newCompositionsSubject = PublishSubject.create();
    private PublishSubject<Map<ShortAlbum, StorageAlbum>> newAlbumsSubject = PublishSubject.create();
    private PublishSubject<Map<String, StorageArtist>> newArtistsSubject = PublishSubject.create();
    private PublishSubject<LongSparseArray<StoragePlayList>> newPlayListsSubject = PublishSubject.create();
    private PublishSubject<List<StoragePlayListItem>> newPlayListItemsSubject = PublishSubject.create();
    private PublishSubject<Map<String, StorageGenre>> newGenreSubject = PublishSubject.create();

    private MediaStorageRepositoryImpl mediaStorageRepository;

    @Before
    public void setUp() {
        when(albumsProvider.getAlbumsObservable()).thenReturn(newAlbumsSubject);
        when(albumsProvider.getAlbums()).thenReturn(new HashMap<>());

        when(genresProvider.getGenresObservable()).thenReturn(newGenreSubject);
        when(genresProvider.getGenres()).thenReturn(new HashMap<>());

        when(artistsProvider.getArtistsObservable()).thenReturn(newArtistsSubject);
        when(artistsProvider.getArtists()).thenReturn(new HashMap<>());

        when(musicProvider.getCompositionsObservable()).thenReturn(newCompositionsSubject);
        when(musicProvider.getCompositions()).thenReturn(new LongSparseArray<>());

        when(playListsProvider.getPlayLists()).thenReturn(storagePlayLists(10));
        when(playListsProvider.getPlayListsObservable()).thenReturn(newPlayListsSubject);
        when(playListsProvider.getPlayListEntriesObservable(1L)).thenReturn(newPlayListItemsSubject);

        when(albumsDao.selectShortAlbumsSet()).thenReturn(Collections.emptySet());

        when(artistsDao.selectAllArtistNames()).thenReturn(new HashSet<>());

        when(genresDao.selectAllGenreNames()).thenReturn(new HashSet<>());

        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(new LongSparseArray<>());

        when(playListsDao.getPlayListsIds()).thenReturn(asList(new IdPair(1L, 1L)));

        mediaStorageRepository = new MediaStorageRepositoryImpl(musicProvider,
                playListsProvider,
                artistsProvider,
                albumsProvider,
                genresProvider,
                compositionsDao,
                playListsDao,
                artistsDao,
                albumsDao,
                genresDao,
                Schedulers.trampoline());
    }

    @Test
    public void changeDatabaseTest() {
        LongSparseArray<StorageComposition> currentCompositions = getFakeStorageCompositionsMap();
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        mediaStorageRepository.runStorageObserver();

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

        mediaStorageRepository.runStorageObserver();

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