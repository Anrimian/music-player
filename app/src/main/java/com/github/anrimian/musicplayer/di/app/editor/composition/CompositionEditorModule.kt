package com.github.anrimian.musicplayer.di.app.editor.composition

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class CompositionEditorModule(private val compositionId: Long) {
    
    @Provides
    fun compositionEditorPresenter(
        interactor: EditorInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = CompositionEditorPresenter(
        compositionId,
        interactor,
        syncInteractor,
        uiScheduler,
        errorParser
    )

}
