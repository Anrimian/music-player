package com.github.anrimian.musicplayer.di.app.library.genres.items

import com.github.anrimian.musicplayer.ui.library.genres.items.GenreItemsPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ GenreItemsModule::class ])
interface GenreItemsComponent {
    fun genreItemsPresenter(): GenreItemsPresenter
}
