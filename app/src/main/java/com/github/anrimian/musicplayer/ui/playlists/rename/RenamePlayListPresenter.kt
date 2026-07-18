package com.github.anrimian.musicplayer.ui.playlists.rename

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class RenamePlayListPresenter(
    private val playListId: Long,
    private val playListsInteractor: PlaylistsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<RenamePlayListView>(uiScheduler, errorParser) {

    private var initialName: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showInputState()
        loadPlayListInfo()
        //compare names and disable apply button, just a little feature
    }

    fun onCompleteInputButtonClicked(playListName: String) {
        viewState.showProgress()
        launch(onError = viewState::showError) {
            playListsInteractor.updatePlaylistName(playListId, playListName)
            viewState.closeScreen()
        }
    }

    private fun loadPlayListInfo() {
        playListsInteractor.getPlaylistFlow(playListId)
            .subscribe(
                onNext = this::onPlayListInfoReceived,
                onError = viewState::showError,
                onComplete = viewState::closeScreen
            )
    }

    private fun onPlayListInfoReceived(playList: Playlist) {
        initialName = playList.name
        viewState.showPlayListName(initialName)
    }

}