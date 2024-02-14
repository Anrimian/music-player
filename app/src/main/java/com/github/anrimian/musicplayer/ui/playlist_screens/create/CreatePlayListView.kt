package com.github.anrimian.musicplayer.ui.playlist_screens.create

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType

private const val CREATE_STATE = "create_state"

interface CreatePlayListView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CREATE_STATE)
    fun showProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CREATE_STATE)
    fun showInputState()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CREATE_STATE)
    fun showError(errorCommand: ErrorCommand)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CREATE_STATE)
    fun onPlayListCreated(playList: PlayList)

}