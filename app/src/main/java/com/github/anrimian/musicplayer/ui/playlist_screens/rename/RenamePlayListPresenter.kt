package com.github.anrimian.musicplayer.ui.playlist_screens.rename

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class RenamePlayListPresenter(private val playListId: Long,
                              private val playListsInteractor: PlayListsInteractor,
                              uiScheduler: Scheduler,
                              errorParser: ErrorParser) 
    : AppPresenter<RenamePlayListView>(uiScheduler, errorParser) {
    
    private var initialName: String? = null
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showInputState()
        loadPlayListInfo()
        //compare names and disable apply button, just a little feature
    }

    fun onCompleteInputButtonClicked(playListName: String) {
        viewState.showProgress()
        playListsInteractor.updatePlayListName(playListId, playListName)
                .subscribeOnUi(viewState::closeScreen, this::onDefaultError)
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showError(errorCommand)
    }

    private fun loadPlayListInfo() {
        playListsInteractor.getPlayListObservable(playListId)
                .subscribeOnUi(this::onPlayListInfoReceived, this::onDefaultError, viewState::closeScreen)
    }

    private fun onPlayListInfoReceived(playList: PlayList) {
        initialName = playList.name
        viewState.showPlayListName(initialName)
    }

}