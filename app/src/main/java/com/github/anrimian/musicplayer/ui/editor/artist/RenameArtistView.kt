package com.github.anrimian.musicplayer.ui.editor.artist

import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

private const val CHANGE_STATE = "change_state"

interface RenameArtistView: MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun showProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun showInputState()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun showError(errorCommand: ErrorCommand)

    @OneExecution
    fun closeScreen()

    @AddToEndSingle
    fun showPreparedFilesCount(processed: Int, total: Int)

    @AddToEndSingle
    fun showDownloadingFileInfo(progressInfo: ProgressInfo)

    @AddToEndSingle
    fun showEditedFilesCount(processed: Int, total: Int)

    @AddToEndSingle
    fun showChangeAllowed(enabled: Boolean)

}