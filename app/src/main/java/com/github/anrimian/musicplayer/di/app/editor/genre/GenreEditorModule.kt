package com.github.anrimian.musicplayer.di.app.editor.genre

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.editor.genre.RenameGenrePresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class GenreEditorModule(private val genreId: Long, private val name: String) {

    @Provides
    fun renameGenrePresenter(
        editorInteractor: EditorInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = RenameGenrePresenter(
        genreId,
        name,
        editorInteractor,
        syncInteractor,
        uiScheduler,
        errorParser
    )

}