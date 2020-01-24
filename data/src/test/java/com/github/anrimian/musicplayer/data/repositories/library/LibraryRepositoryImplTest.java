package com.github.anrimian.musicplayer.data.repositories.library;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.folders.MusicFolderDataSource;
import com.github.anrimian.musicplayer.data.repositories.settings.SettingsRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.composition.order.OrderType;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getTestFolderSingle;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LibraryRepositoryImplTest {

    private StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private CompositionsDaoWrapper compositionsDao = mock(CompositionsDaoWrapper.class);
    private ArtistsDaoWrapper artistsDao = mock(ArtistsDaoWrapper.class);
    private AlbumsDaoWrapper albumsDao = mock(AlbumsDaoWrapper.class);
    private GenresDaoWrapper genresDao = mock(GenresDaoWrapper.class);
    private MusicFolderDataSource musicFolderDataSource = mock(MusicFolderDataSource.class);
    private SettingsRepositoryImpl settingsPreferences = mock(SettingsRepositoryImpl.class);
    private Scheduler scheduler = Schedulers.trampoline();

    private LibraryRepository musicProviderRepository = new LibraryRepositoryImpl(
            storageMusicDataSource,
            compositionsDao,
            artistsDao,
            albumsDao,
            genresDao,
            musicFolderDataSource,
            settingsPreferences,
            scheduler
    );

    @Before
    public void setUp() {
        when(settingsPreferences.getFolderOrder()).thenReturn(new Order(OrderType.ALPHABETICAL, false));
    }

    @Test
    public void getAllCompositionsInPath() {
        Composition compositionOne = fakeComposition(1, "1");
        Composition compositionTwo = fakeComposition(2, "2");

        when(musicFolderDataSource.getCompositionsInPath(null))
                .thenReturn(getTestFolderSingle(
                        new FolderFileSource("basic", 1, new Date(0), new Date(0)),
                        new MusicFileSource(compositionOne)
                ));

        when(musicFolderDataSource.getCompositionsInPath("basic"))
                .thenReturn(getTestFolderSingle(
                        new MusicFileSource(compositionTwo)
                ));

        musicProviderRepository.getAllCompositionsInPath(null)
                .test()
                .assertValue(compositions -> {
                    assertEquals(2, compositions.size());
                    assertEquals(compositionTwo, compositions.get(0));
                    assertEquals(compositionOne, compositions.get(1));
                    return true;
                });

    }

    @Test
    public void getAllCompositionsInPathSimple() {
        Composition compositionOne = fakeComposition(1, "1");
        Composition compositionTwo = fakeComposition(2, "2");

        when(musicFolderDataSource.getCompositionsInPath(null))
                .thenReturn(getTestFolderSingle(
                        new MusicFileSource(compositionOne),
                        new MusicFileSource(compositionTwo)
                ));

        musicProviderRepository.getAllCompositionsInPath(null)
                .test()
                .assertValue(compositions -> {
                    assertEquals(2, compositions.size());
                    assertEquals(compositionOne, compositions.get(0));
                    assertEquals(compositionTwo, compositions.get(1));
                    return true;
                });

    }

}