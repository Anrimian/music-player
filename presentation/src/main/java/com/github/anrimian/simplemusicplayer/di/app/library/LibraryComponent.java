package com.github.anrimian.simplemusicplayer.di.app.library;

import com.github.anrimian.simplemusicplayer.di.app.library.storage.StorageLibraryComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.storage.StorageLibraryModule;
import com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.PlayerPresenter;

import dagger.Subcomponent;

/**
 * Created on 29.10.2017.
 */

@LibraryScope
@Subcomponent(modules = LibraryModule.class)
public interface LibraryComponent {

    StorageLibraryComponent storageLibraryComponent(StorageLibraryModule storageLibraryModule);

    PlayerPresenter libraryPresenter();
}
