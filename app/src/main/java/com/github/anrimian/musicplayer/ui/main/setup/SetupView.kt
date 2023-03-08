package com.github.anrimian.musicplayer.ui.main.setup

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.OneExecution

/**
 * Created on 19.10.2017.
 */

private const val SCREEN_STATE = "screen_state"

interface SetupView: MvpView {

    @OneExecution
    fun requestFilesPermissions()

    @OneExecution
    fun goToMainScreen()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = SCREEN_STATE)
    fun showDeniedPermissionMessage()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = SCREEN_STATE)
    fun showStub()

    @OneExecution
    fun startSystemServices()

}