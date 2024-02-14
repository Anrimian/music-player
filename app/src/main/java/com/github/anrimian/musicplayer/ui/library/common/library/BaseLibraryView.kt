package com.github.anrimian.musicplayer.ui.library.common.library

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.OneExecution

interface BaseLibraryView : MvpView {

    @OneExecution
    fun onCompositionsAddedToPlayNext(compositions: List<Composition>)

    @OneExecution
    fun onCompositionsAddedToQueue(compositions: List<Composition>)

    @OneExecution
    fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>)

    @OneExecution
    fun showPlaylistDuplicateEntryDialog(
        compositions: Collection<Composition>,
        hasNonDuplicates: Boolean,
        playList: PlayList,
        isDuplicateCheckEnabled: Boolean
    )

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

}