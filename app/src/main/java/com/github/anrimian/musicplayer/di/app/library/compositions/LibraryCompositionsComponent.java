package com.github.anrimian.musicplayer.di.app.library.compositions;

import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsPresenter;
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersPresenter;

import dagger.Subcomponent;

/**
 * Created on 31.10.2017.
 */
@Subcomponent(modules = LibraryCompositionsModule.class)
public interface LibraryCompositionsComponent {

    LibraryCompositionsPresenter libraryCompositionsPresenter();
}
