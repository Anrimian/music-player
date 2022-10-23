package com.github.anrimian.musicplayer.di.app.editor.artist

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.editor.artist.RenameArtistPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class ArtistEditorModule(private val artistId: Long, private val name: String) {

    @Provides
    fun compositionEditorPresenter(
        editorInteractor: EditorInteractor,
        syncInteractor: SyncInteractor<*, *, Long>,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = RenameArtistPresenter(
        artistId,
        name,
        editorInteractor,
        syncInteractor,
        uiScheduler,
        errorParser
    )

}