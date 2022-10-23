package com.github.anrimian.musicplayer.ui.editor.common

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.filesync.models.state.file.Downloading
import com.github.anrimian.musicplayer.domain.utils.rx.doOnFirst
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

fun performFilesChangeAction(
    syncInteractor: SyncInteractor<*, *, Long>,
    uiScheduler: Scheduler,
    onFilesPrepared: (Int) -> Unit,
    onFileDownloading: (ProgressInfo) -> Unit,
    onFilesEdited: (Int) -> Unit,
    action: (
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>
    ) -> Completable
): Completable {
    return Single.create<Completable> { emitter ->
        val downloadingSubject = BehaviorSubject.create<Long>()
        val editingSubject = BehaviorSubject.create<Long>()
        var preparedFilesCount = 0
        var editedFileCount = 0

        val disposable = CompositeDisposable()
        disposable.add(
            downloadingSubject
                .ignoreElements()
                .andThen(editingSubject)
                .observeOn(uiScheduler)
                .subscribe { onFilesEdited(++editedFileCount) }
        )
        disposable.add(
            downloadingSubject
                .switchMap { id ->
                    syncInteractor.getFileSyncStateObservable(id)
                        .observeOn(uiScheduler)
                        .filter { state -> state is Downloading }
                        .doOnFirst { onFilesPrepared(++preparedFilesCount) }
                }
                .subscribe { fileSyncState ->
                    if (fileSyncState is Downloading) {
                        onFileDownloading(fileSyncState.getProgress())
                    }
                }
        )
        val completable = action(downloadingSubject, editingSubject)
            .doFinally { disposable.dispose() }
        emitter.onSuccess(completable)
    }.flatMapCompletable { completable -> completable }
        .observeOn(uiScheduler)
}