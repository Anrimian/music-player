package com.github.anrimian.musicplayer.di.app.library.albums.items

import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsPresenter
import dagger.Subcomponent

@Subcomponent(modules = [AlbumItemsModule::class])
interface AlbumItemsComponent {

    fun albumItemsPresenter(): AlbumItemsPresenter

}