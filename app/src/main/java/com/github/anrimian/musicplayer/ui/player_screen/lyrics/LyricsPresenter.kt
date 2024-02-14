package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class LyricsPresenter(
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
): AppPresenter<LyricsView>(uiScheduler, errorParser) {

    private var currentComposition: Composition? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        libraryPlayerInteractor.getCurrentQueueItemObservable()
            .unsafeSubscribeOnUi(this::onCurrentQueueItemChanged)
        libraryPlayerInteractor.getCurrentCompositionLyrics()
            .unsafeSubscribeOnUi(this::onLyricsReceived)
    }

    fun onEditLyricsClicked() {
        if (currentComposition != null) {
            viewState.showEditLyricsScreen(currentComposition!!.id)
        }
    }

    private fun onLyricsReceived(lyrics: Optional<String>) {
        viewState.showLyrics(lyrics.value)
    }

    private fun onCurrentQueueItemChanged(event: PlayQueueEvent) {
        currentComposition = event.playQueueItem?.composition
        viewState.resetTextPosition()
    }

}