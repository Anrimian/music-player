package com.github.anrimian.musicplayer.ui.common.dialogs.missing

import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface MissingFilesView : MvpView {

    @AddToEndSingle
    fun showMissingAudioFiles(audioFiles: List<AudioFileInfo>)

    @AddToEndSingle
    fun showRestoreInProgress(show: Boolean)

    @AddToEndSingle
    fun showRestoreErrors(errors: Map<AudioFileInfo, ErrorCommand>)

    @OneExecution
    fun showConfirmDeleteMissingCompositionsDialog(files: List<AudioFileInfo>)

    @OneExecution
    fun onMissingCompositionsDeleted(deletedCompositions: List<DeletedComposition>)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

}