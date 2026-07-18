package com.github.anrimian.musicplayer.ui.library.common.library

import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.OneExecution

interface BaseLibraryView : MvpView {

    @OneExecution
    fun onCompositionsAddedToPlayNext(compositions: List<CompositionModel>)

    @OneExecution
    fun onCompositionsAddedToQueue(compositions: List<CompositionModel>)

    @OneExecution
    fun showAddingToPlaylistComplete(playList: Playlist, compositions: List<CompositionModel>)

    @OneExecution
    fun showPlaylistDuplicateEntryDialog(
        compositions: Collection<CompositionModel>,
        hasNonDuplicates: Boolean,
        playList: Playlist,
        isDuplicateCheckEnabled: Boolean
    )

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

}