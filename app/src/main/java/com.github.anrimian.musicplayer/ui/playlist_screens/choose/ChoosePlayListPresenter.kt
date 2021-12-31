package com.github.anrimian.musicplayer.ui.playlist_screens.choose

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class ChoosePlayListPresenter(
        private val playListsInteractor: PlayListsInteractor,
        uiScheduler: Scheduler,
        errorParser: ErrorParser
) : AppPresenter<ChoosePlayListView>(uiScheduler, errorParser) {
    
    private var slideOffset = 0f

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showBottomSheetSlided(0f)
        subscribeOnPlayLists()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenterDisposable.dispose()
    }

    fun onBottomSheetSlided(slideOffset: Float) {
        this.slideOffset = slideOffset
        viewState.showBottomSheetSlided(slideOffset)
    }

    fun onDeletePlayListButtonClicked(playList: PlayList) {
        viewState.showConfirmDeletePlayListDialog(playList)
    }

    fun onDeletePlayListDialogConfirmed(playList: PlayList) {
        playListsInteractor.deletePlayList(playList.id)
                .subscribeOnUi({ onPlayListDeleted(playList) }, this::onPlayListDeletingError)
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
        playListsInteractor.playListsObservable
                .unsafeSubscribeOnUi(this::onPlayListsReceived)
    }

    private fun onPlayListsReceived(list: List<PlayList>) {
        viewState.updateList(list)
        if (list.isEmpty()) {
            viewState.showEmptyList()
        } else {
            viewState.showList()
        }
        viewState.showBottomSheetSlided(slideOffset)
    }
}