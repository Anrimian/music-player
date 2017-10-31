package com.github.anrimian.simplemusicplayer.ui.library.storage;

import com.github.anrimian.simplemusicplayer.domain.business.music.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.utils.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.utils.error.parser.ErrorParser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 29.10.2017.
 */
public class StorageLibraryPresenterTest {

    private StorageLibraryInteractor interactor;
    private StorageLibraryView view;
    private Scheduler uiScheduler = Schedulers.single();

    private ErrorParser errorParser;

    @Before
    public void setUp() throws Exception {
        List<MusicFileSource> musicFileSources = new ArrayList<>();
        musicFileSources.add(new MusicFileSource());
        musicFileSources.add(new MusicFileSource());
        musicFileSources.add(new MusicFileSource());
        musicFileSources.add(new MusicFileSource());

        view = mock(StorageLibraryView.class);

        interactor = mock(StorageLibraryInteractor.class);
        when(interactor.getMusicInPath(any())).thenReturn(Single.just(musicFileSources));

        errorParser = mock(ErrorParser.class);
        when(errorParser.parseError(any())).thenReturn(mock(ErrorCommand.class));
    }

    @Test
    public void testPresenterLoadList() {
        StorageLibraryPresenter presenter = new StorageLibraryPresenter(null, interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        verify(view).hideBackPathButton();
        verify(view).showLoading();
        verify(view, never()).showEmptyList();
        verify(view).showMusicList(anyListOf(MusicFileSource.class));
    }

    @Test
    public void testPresenterLoadEmptyList() {
        StorageLibraryInteractor interactor = mock(StorageLibraryInteractor.class);
        when(interactor.getMusicInPath(any())).thenReturn(Single.just(new ArrayList<>()));

        StorageLibraryPresenter presenter = new StorageLibraryPresenter(null, interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        verify(view).hideBackPathButton();
        verify(view).showLoading();
        verify(view).showEmptyList();
        verify(view, never()).showMusicList(anyListOf(MusicFileSource.class));
    }

    @Test
    public void testPresenterLoadingError() {
        StorageLibraryInteractor interactor = mock(StorageLibraryInteractor.class);
        when(interactor.getMusicInPath(any())).thenReturn(Single.error(new Exception()));

        StorageLibraryPresenter presenter = new StorageLibraryPresenter(null, interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        verify(view).hideBackPathButton();
        verify(view).showLoading();
        verify(view).showError(any(ErrorCommand.class));
    }

    @Test
    public void testOnMusicSourceClicked() {
        StorageLibraryPresenter presenter = new StorageLibraryPresenter(null, interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        MusicFileSource musicFileSource = new MusicFileSource();
        musicFileSource.setComposition(new Composition());
        presenter.onMusicSourceClicked(musicFileSource);
        verify(interactor).playMusic(eq(musicFileSource));

        String path = "some";
        musicFileSource.setComposition(null);
        musicFileSource.setPath(path);
        presenter.onMusicSourceClicked(musicFileSource);
        verify(view).goToMusicStorageScreen(eq(path));

        presenter = new StorageLibraryPresenter("some", interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        musicFileSource.setComposition(null);
        musicFileSource.setPath("some2");
        presenter.onMusicSourceClicked(musicFileSource);
        verify(view).showBackPathButton("some");
        verify(view).goToMusicStorageScreen(eq("some/some2"));
    }

    @Test
    public void testOnPlayAllButtonClicked() {
        String path = null;
        StorageLibraryPresenter presenter = new StorageLibraryPresenter(path, interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        presenter.onPlayAllButtonClicked();
        verify(interactor).playAllMusicInPath(eq(path));
    }


}