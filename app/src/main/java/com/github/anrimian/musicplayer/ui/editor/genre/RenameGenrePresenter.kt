package com.github.anrimian.musicplayer.ui.editor.genre

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.editor.batch.BatchEditorPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject

class RenameGenrePresenter(
    private val genreId: Long,
    initialName: String,
    private val editorInteractor: EditorInteractor,
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): BatchEditorPresenter<RenameGenreView>(
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
        return editorInteractor.updateGenreName(
            currentText,
            genreId,
            affectedFilesCount,
            downloadingSubject,
            editingSubject
        )
    }

}