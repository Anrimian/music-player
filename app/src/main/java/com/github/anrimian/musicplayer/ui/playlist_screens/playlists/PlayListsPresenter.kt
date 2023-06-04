package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyExistsException
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistNotCompletelyImportedException
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class PlayListsPresenter(
    private val playListsInteractor: PlayListsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): AppPresenter<PlayListsView>(uiScheduler, errorParser) {

    private var playLists: List<PlayList> = ArrayList()

    private val playlistsForExport = ArrayList<PlayList>()

    private var lastPlaylistForImport: FileReference? = null

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
            .subscribe({ onPlayListDeleted(playList) }, viewState::showDeletePlayListError)
    }

    fun onFragmentMovedToTop() {
        playListsInteractor.setSelectedPlayListScreen(0)
    }

    fun onChangePlayListNameButtonClicked(playList: PlayList) {
        viewState.showEditPlayListNameDialog(playList)
    }

    fun onExportPlaylistClicked(playList: PlayList) {
        playlistsForExport.clear()
        playlistsForExport.add(playList)
        viewState.launchPickFolderScreen()
    }

    fun onFolderForExportSelected(folder: FileReference) {
        playListsInteractor.exportPlaylistsToFolder(playlistsForExport, folder)
            .subscribe(this::onPlaylistsExported, viewState::showErrorMessage)
    }

    fun onPlaylistFileReceived(file: FileReference) {
        lastPlaylistForImport = file
        importPlaylistFile(file, false)
    }

    fun onOverwritePlaylistConfirmed() {
        lastPlaylistForImport?.let { ref -> importPlaylistFile(ref, true) }
    }

    private fun importPlaylistFile(file: FileReference, overwriteExisting: Boolean) {
        playListsInteractor.importPlaylistFile(file, overwriteExisting)
            .justSubscribeOnUi(this::onPlaylistImported, this::onPlaylistFileImportError)
    }

    private fun onPlaylistImported(playlistId: Long) {
        viewState.launchPlayListScreen(playlistId)
    }

    private fun onPlaylistFileImportError(throwable: Throwable) {
        when (throwable) {
            is PlayListAlreadyExistsException -> viewState.showOverwritePlaylistDialog()
            is PlaylistNotCompletelyImportedException -> {
                viewState.showNotCompletelyImportedPlaylistDialog(
                    throwable.playlistId,
                    throwable.notFoundFilesCount
                )
            }
            else -> viewState.showErrorMessage(errorParser.parseError(throwable))
        }
    }

    private fun onPlaylistsExported() {
        viewState.showPlaylistExportSuccess(playlistsForExport)
        playlistsForExport.clear()
    }

    private fun onPlayListDeleted(playList: PlayList) {
        viewState.showPlayListDeleteSuccess(playList)
    }

    private fun subscribeOnPlayLists() {
        viewState.showLoading()
        playListsInteractor.getPlayListsObservable().unsafeSubscribeOnUi(this::onPlayListsReceived)
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
                val listPosition = playListsInteractor.getSavedListPosition()
                if (listPosition != null) {
                    viewState.restoreListPosition(listPosition)
                }
            }
        }
    }
}