package com.github.anrimian.musicplayer.ui.editor.album

import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val DISPLAY_ALBUM_STATE = "display_album_state"
private const val CHANGE_STATE = "change_state"

interface AlbumEditorView : MvpView {

    @OneExecution
    fun closeScreen()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = DISPLAY_ALBUM_STATE)
    fun showAlbumLoadingError(errorCommand: ErrorCommand)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = DISPLAY_ALBUM_STATE)
    fun showAlbum(album: Album)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun showRenameProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun hideRenameProgress()

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @Skip
    fun showEnterAuthorDialog(album: Album, hints: Array<String>?)

    @Skip
    fun showEnterNameDialog(album: Album)

    @AddToEndSingle
    fun showPreparedFilesCount(processed: Int, total: Int)

    @AddToEndSingle
    fun showDownloadingFileInfo(progressInfo: ProgressInfo)

    @AddToEndSingle
    fun showEditedFilesCount(processed: Int, total: Int)

}