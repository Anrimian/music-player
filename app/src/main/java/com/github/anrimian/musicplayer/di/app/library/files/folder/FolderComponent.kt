package com.github.anrimian.musicplayer.di.app.library.files.folder;

import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersPresenter;

import dagger.Subcomponent;

/**
 * Created on 31.10.2017.
 */
@Subcomponent(modules = FolderModule.class)
public interface FolderComponent {

    LibraryFoldersPresenter storageLibraryPresenter();
}
