package com.github.anrimian.musicplayer.di.app.library.files

import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderComponent
import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderModule
import com.github.anrimian.musicplayer.ui.library.folders.root.FolderRootPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ LibraryFilesModule::class ])
@LibraryFilesScope
interface LibraryFilesComponent {
    fun folderRootPresenter(): FolderRootPresenter
    fun folderComponent(module: FolderModule): FolderComponent
}
