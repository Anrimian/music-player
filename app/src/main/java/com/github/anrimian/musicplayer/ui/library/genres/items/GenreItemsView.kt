package com.github.anrimian.musicplayer.ui.library.genres.items

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface GenreItemsView : BaseLibraryCompositionsView<Composition> {

    @AddToEndSingle
    fun showGenreInfo(genre: Genre)

    @OneExecution
    fun closeScreen()

    @Skip
    fun showRenameGenreDialog(genre: Genre)

}