package com.github.anrimian.musicplayer.ui.library.artists.items

import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface ArtistItemsView : BaseLibraryCompositionsView {

    @AddToEndSingle
    fun showArtistInfo(artist: Artist)

    @AddToEndSingle
    fun showArtistAlbums(albums: List<Album>)

    @OneExecution
    fun closeScreen()

    @Skip
    fun showRenameArtistDialog(artist: Artist)

}