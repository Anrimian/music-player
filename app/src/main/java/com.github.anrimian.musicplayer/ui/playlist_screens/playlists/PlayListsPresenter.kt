package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler
import java.util.*

class PlayListsPresenter(
        private val playListsInteractor: PlayListsInteractor,
        uiScheduler: Scheduler,
        errorParser: ErrorParser
) : AppPresenter<PlayListsView>(uiScheduler, errorParser) {
    
    private var playLists: List<PlayList> = ArrayList()
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnPlayLists()
    }

    fun onStop(listPosition: ListPosition?) {
        playListsInteractor.saveListPosition(listPosition)
    }

    fun onDeletePlayListButtonClicked(playList: PlayList) {
        viewState.showConfirmDeletePlayListDialog(playList)
    }

    fun onDeletePlayListDialogConfirmed(playList: PlayList) {
        playListsInteractor.deletePlayList(playList.id)
                .subscribeOnUi({ onPlayListDeleted(playList) }, this::onPlayListDeletingError)
    }

    fun onFragmentMovedToTop() {
        playListsInteractor.setSelectedPlayListScreen(0)
    }

    fun onChangePlayListNameButtonClicked(playList: PlayList) {
        viewState.showEditPlayListNameDialog(playList)
    }

    private fun onPlayListDeletingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeletePlayListError(errorCommand)
    }

    private fun onPlayListDeleted(playList: PlayList) {
        viewState.showPlayListDeleteSuccess(playList)
    }

    private fun subscribeOnPlayLists() {
        viewState.showLoading()
        playListsInteractor.playListsObservable.unsafeSubscribeOnUi(this::onPlayListsReceived)
    }

    private fun onPlayListsReceived(list: List<PlayList>) {
        val firstReceive = playLists.isEmpty()
        playLists = list
        viewState.updateList(list)
        if (list.isEmpty()) {
            viewState.showEmptyList()
        } else {
            viewState.showList()
            if (firstReceive) {
                val listPosition = playListsInteractor.savedListPosition
                if (listPosition != null) {
                    viewState.restoreListPosition(listPosition)
                }
            }
        }
    }
}