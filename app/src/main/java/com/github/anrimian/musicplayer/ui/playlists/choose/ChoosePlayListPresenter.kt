package com.github.anrimian.musicplayer.ui.playlists.choose

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler
import kotlinx.coroutines.rx3.asFlow

class ChoosePlayListPresenter(
    private val playListsInteractor: PlaylistsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<ChoosePlayListView>(uiScheduler, errorParser) {

    private var slideOffset = 0f

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showBottomSheetSlided(0f)
        subscribeOnPlayLists()
    }

    fun onTryAgainButtonClicked() {
        subscribeOnPlayLists()
    }

    fun onBottomSheetSlided(slideOffset: Float) {
        this.slideOffset = slideOffset
        viewState.showBottomSheetSlided(slideOffset)
    }

    fun onDeletePlayListButtonClicked(playList: Playlist) {
        viewState.showConfirmDeletePlayListDialog(playList)
    }

    fun onDeletePlayListDialogConfirmed(playList: Playlist) {
        launch(onError = viewState::showDeletePlayListError) {
            playListsInteractor.deletePlaylist(playList.id)
            viewState.showPlayListDeleteSuccess(playList)
        }
    }

    fun onChangePlayListNameButtonClicked(playList: Playlist) {
        viewState.showEditPlayListNameDialog(playList)
    }

    private fun subscribeOnPlayLists() {
        viewState.showLoading()
        playListsInteractor.getPlaylistsObservable().asFlow()
            .subscribe(
                onNext = this::onPlayListsReceived,
                onError = viewState::showErrorState
            )
    }

    private fun onPlayListsReceived(list: List<Playlist>) {
        viewState.updateList(list)
        if (list.isEmpty()) {
            viewState.showEmptyList()
        } else {
            viewState.showList()
        }
        viewState.showBottomSheetSlided(slideOffset)
    }
}