package com.github.anrimian.musicplayer.di.app.library.compositions

import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsPresenter
import dagger.Subcomponent

/**
 * Created on 31.10.2017.
 */
@Subcomponent(modules = [ LibraryCompositionsModule::class ])
interface LibraryCompositionsComponent {
    fun libraryCompositionsPresenter(): LibraryCompositionsPresenter
}
