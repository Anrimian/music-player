package com.github.anrimian.musicplayer.ui.playlists.create

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class CreatePlayListPresenter(
    private val playListsInteractor: PlaylistsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<CreatePlayListView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showInputState()
    }

    fun onCompleteInputButtonClicked(playListName: String) {
        viewState.showProgress()
        playListsInteractor.createPlaylist(playListName)
            .launchOnUi(viewState::onPlayListCreated, viewState::showError)
    }

}