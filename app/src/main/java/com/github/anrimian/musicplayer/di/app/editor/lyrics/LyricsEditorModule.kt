package com.github.anrimian.musicplayer.di.app.editor.lyrics

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.editor.lyrics.LyricsEditorPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class LyricsEditorModule(private val compositionId: Long) {

    @Provides
    fun lyricsEditorPresenter(
        editorInteractor: EditorInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = LyricsEditorPresenter(compositionId, editorInteractor, uiScheduler, errorParser)
}