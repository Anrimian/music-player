package com.github.anrimian.musicplayer.ui.playlist_screens.create

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class CreatePlayListPresenter(
    private val playListsInteractor: PlayListsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<CreatePlayListView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showInputState()
    }

    fun onCompleteInputButtonClicked(playListName: String) {
        viewState.showProgress()
        playListsInteractor.createPlayList(playListName)
            .launchOnUi(viewState::onPlayListCreated, viewState::showError)
    }

}