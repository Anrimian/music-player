package com.github.anrimian.simplemusicplayer.ui.library.storage;

import com.github.anrimian.simplemusicplayer.domain.business.music.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.utils.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.utils.error.parser.ErrorParser;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
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
        List<FileSource> musicFileSources = new ArrayList<>();
        musicFileSources.add(mock(FileSource.class));
        musicFileSources.add(mock(FileSource.class));
        musicFileSources.add(mock(FileSource.class));
        musicFileSources.add(mock(FileSource.class));

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
        verify(view).showMusicList(anyListOf(FileSource.class));
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
        verify(view, never()).showMusicList(anyListOf(FileSource.class));
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
    public void testOnMusicClicked() {
        StorageLibraryPresenter presenter = new StorageLibraryPresenter(null, interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        Composition composition = new Composition();
        presenter.onCompositionClicked(composition);
        verify(interactor).playMusic(eq(composition));
    }

    @Test
    public void testOnFolderClicked() {
        StorageLibraryPresenter presenter = new StorageLibraryPresenter(null, interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        String path = "some";
        presenter.onFolderClicked(path);
        verify(view).goToMusicStorageScreen(eq(path));

        presenter = new StorageLibraryPresenter("some", interactor, errorParser, uiScheduler);
        presenter.attachView(view);
        verify(view).showBackPathButton("some");

        path = "some/some2";
        presenter.onFolderClicked(path);
        verify(view).goToMusicStorageScreen(eq("some/some2"));
    }

    @Test
    public void testShowBackPathButton() {
        StorageLibraryPresenter presenter = new StorageLibraryPresenter("root/some/some2", interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        verify(view).showBackPathButton("root/some/some2");
    }

    @Test
    public void testBackPathButtonClicked() {
        StorageLibraryPresenter presenter = new StorageLibraryPresenter("root/some/some2", interactor, errorParser, uiScheduler);
        presenter.attachView(view);

        presenter.onBackPathButtonClicked();
        verify(view).goBackToMusicStorageScreen("root/some");
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