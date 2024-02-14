package com.github.anrimian.musicplayer.ui.editor.artist

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.editor.batch.BatchEditorPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject

class RenameArtistPresenter(
    private val artistId: Long,
    initialName: String,
    private val editorInteractor: EditorInteractor,
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): BatchEditorPresenter<RenameArtistView>(
    initialName,
    syncInteractor,
    uiScheduler,
    errorParser
) {

    override fun performEditAction(
        currentText: String,
        affectedFilesCount: (Int) -> Unit,
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>,
    ): Completable {
        return editorInteractor.updateArtistName(
            currentText,
            artistId,
            affectedFilesCount,
            downloadingSubject,
            editingSubject
        )
    }

}