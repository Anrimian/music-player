package com.github.anrimian.simplemusicplayer.di.app.library;

import com.github.anrimian.simplemusicplayer.di.app.library.compositions.LibraryCompositionsComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.compositions.LibraryCompositionsModule;
import com.github.anrimian.simplemusicplayer.di.app.library.files.LibraryFilesComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.files.LibraryFilesModule;
import com.github.anrimian.simplemusicplayer.ui.player_screen.PlayerPresenter;

import dagger.Subcomponent;

/**
 * Created on 29.10.2017.
 */

@LibraryScope
@Subcomponent(modules = LibraryModule.class)
public interface LibraryComponent {

    LibraryFilesComponent libraryFilesComponent(LibraryFilesModule module);
    LibraryCompositionsComponent libraryCompositionsComponent(LibraryCompositionsModule module);

    PlayerPresenter playerPresenter();
}
