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
    private var playListInMenu: PlayList? = null
    private var playListToDelete: PlayList? = null
    
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

    fun onPlayListLongClick(playList: PlayList) {
        playListInMenu = playList
        viewState.showPlayListMenu(playList)
    }

    fun onDeletePlayListButtonClicked() {
        playListToDelete = playListInMenu
        viewState.showConfirmDeletePlayListDialog(playListToDelete)
        playListInMenu = null
    }

    fun onDeletePlayListDialogConfirmed() {
        playListsInteractor.deletePlayList(playListToDelete!!.id)
                .subscribeOnUi(this::onPlayListDeleted, this::onPlayListDeletingError)
    }

    fun onChangePlayListNameButtonClicked() {
        viewState.showEditPlayListNameDialog(playListInMenu)
    }

    private fun onPlayListDeletingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeletePlayListError(errorCommand)
        playListToDelete = null
    }

    private fun onPlayListDeleted() {
        viewState.showPlayListDeleteSuccess(playListToDelete)
        playListToDelete = null
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