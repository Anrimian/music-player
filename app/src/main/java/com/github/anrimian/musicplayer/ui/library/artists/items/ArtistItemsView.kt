package com.github.anrimian.musicplayer.ui.library.artists.items

import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val RENAME_STATE = "rename_state"

interface ArtistItemsView : BaseLibraryCompositionsView {

    @AddToEndSingle
    fun showArtistInfo(artist: Artist)

    @AddToEndSingle
    fun showArtistAlbums(albums: List<Album>)

    @OneExecution
    fun closeScreen()

    @Skip
    fun showRenameArtistDialog(artist: Artist)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = RENAME_STATE)
    fun showRenameProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = RENAME_STATE)
    fun hideRenameProgress()

}