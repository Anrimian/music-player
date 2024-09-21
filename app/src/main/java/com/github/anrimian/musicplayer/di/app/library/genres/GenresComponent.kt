package com.github.anrimian.musicplayer.di.app.library.genres

import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsComponent
import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsModule
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ GenresModule::class ])
interface GenresComponent {

    fun genreItemsComponent(module: GenreItemsModule): GenreItemsComponent
    fun genresListPresenter(): GenresListPresenter

}
