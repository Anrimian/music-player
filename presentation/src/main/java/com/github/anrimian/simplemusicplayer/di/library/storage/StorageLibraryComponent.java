package com.github.anrimian.simplemusicplayer.di.library.storage;

import com.github.anrimian.simplemusicplayer.ui.storage_library_screen.StorageLibraryPresenter;

import dagger.Subcomponent;

/**
 * Created on 31.10.2017.
 */
@Subcomponent(modules = StorageLibraryModule.class)
public interface StorageLibraryComponent {

    StorageLibraryPresenter storageLibraryPresenter();
}
