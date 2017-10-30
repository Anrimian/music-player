package com.github.anrimian.simplemusicplayer.ui.library.storage;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created on 29.10.2017.
 */
public class StorageLibraryPresenterTest {

    @Test
    public void testPresenter() {

        StorageLibraryView view = mock(StorageLibraryView.class);

        StorageLibraryPresenter presenter = new StorageLibraryPresenter(null);
        presenter.attachView(view);

        verify(view).showEmptyList();


    }

}