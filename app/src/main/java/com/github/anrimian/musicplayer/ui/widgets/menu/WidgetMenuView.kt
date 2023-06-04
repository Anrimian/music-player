package com.github.anrimian.musicplayer.ui.widgets.menu

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.OneExecution

private const val COMPOSITION_STATE = "composition_state"

interface WidgetMenuView: MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = COMPOSITION_STATE)
    fun showComposition(composition: Composition)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = COMPOSITION_STATE)
    fun showCompositionError(errorCommand: ErrorCommand)

    @OneExecution
    fun shareComposition(composition: Composition)

    @OneExecution
    fun showConfirmDeleteDialog(composition: Composition)

    @OneExecution
    fun closeScreen()

    @OneExecution
    fun showDeleteCompositionMessage(composition: DeletedComposition)

    @OneExecution
    fun showDeleteCompositionError(errorCommand: ErrorCommand)

}