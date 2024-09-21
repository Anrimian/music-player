package com.github.anrimian.musicplayer.di.app.editor.album

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.editor.album.AlbumEditorPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class AlbumEditorModule(private val albumId: Long) {
    
    @Provides
    fun compositionEditorPresenter(
        interactor: EditorInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = AlbumEditorPresenter(
        albumId,
        interactor,
        syncInteractor,
        uiScheduler,
        errorParser
    )

}
