package com.github.anrimian.musicplayer.ui.common.dialogs.share

import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface ShareCompositionsView: MvpView {

    @OneExecution
    fun showShareDialog(sources: ArrayList<CompositionContentSource>)

    @AddToEndSingle
    fun showShareError(errorCommand: ErrorCommand?)

    @AddToEndSingle
    fun showProcessedFileCount(processed: Int, total: Int)

    @AddToEndSingle
    fun showDownloadingFileInfo(progressInfo: ProgressInfo)

}