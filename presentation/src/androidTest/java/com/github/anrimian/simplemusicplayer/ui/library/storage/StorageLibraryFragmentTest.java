package com.github.anrimian.simplemusicplayer.ui.library.storage;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 29.10.2017.
 */

@RunWith(AndroidJUnit4.class)
public class StorageLibraryFragmentTest {

    private StorageLibraryPresenter presenter;
    private StorageLibraryFragment fragment;

    @Before
    public void setUp() {
        presenter = mock(StorageLibraryPresenter.class);

        fragment = StorageLibraryFragment.newInstance(null);
        when(fragment.providePresenter()).thenReturn(presenter);
    }

    @Test
    public void testTest() {
        presenter.onFirstViewAttach();

//        Assert.assertNotNull();
    }


}