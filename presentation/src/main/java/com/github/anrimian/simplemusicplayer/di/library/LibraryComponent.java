package com.github.anrimian.simplemusicplayer.di.library;

import com.github.anrimian.simplemusicplayer.ui.library.storage.StorageLibraryPresenter;

import dagger.Subcomponent;

/**
 * Created on 29.10.2017.
 */

@LibraryScope
@Subcomponent(modules = LibraryModule.class)
public interface LibraryComponent {

    void inject(StorageLibraryPresenter storageLibraryPresenter);
}
