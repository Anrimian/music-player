package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyExistsException
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistNotCompletelyImportedException
import com.github.anrimian.musicplayer.data.utils.rx.mapError
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.share.models.ReceiveCompositionsForSendException
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class PlayListsPresenter(
    private val playListsInteractor: PlayListsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): BaseLibraryPresenter<PlayListsView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private var playlistsDisposable: Disposable? = null

    private var playLists: List<PlayList> = ArrayList()
    private val selectedPlaylists = LinkedHashSet<PlayList>()

    private val playlistsForExport = ArrayList<PlayList>()

    private var lastPlaylistForImport: FileReference? = null

    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnPlayLists()
    }

    fun onStop(listPosition: ListPosition?) {
        playListsInteractor.saveListPosition(listPosition)
    }

    fun onPlaylistClicked(position: Int, playList: PlayList) {
        if (selectedPlaylists.isEmpty()) {
            viewState.launchPlayListScreen(playList.id)
            return
        }
        if (selectedPlaylists.contains(playList)) {
            selectedPlaylists.remove(playList)
            viewState.onPlaylistUnselected(playList, position)
        } else {
            selectedPlaylists.add(playList)
            viewState.onPlaylistSelected(playList, position)
        }
        viewState.showSelectionMode(selectedPlaylists)
    }
    
    fun onPlaylistLongClicked(position: Int, playList: PlayList) {
        selectedPlaylists.add(playList)
        viewState.showSelectionMode(selectedPlaylists)
        viewState.onPlaylistSelected(playList, position)
    }

    fun onPlayAllSelectedClicked() {
        playSelectedPlaylists()
    }

    fun onSelectAllButtonClicked() {
        selectedPlaylists.clear()
        selectedPlaylists.addAll(playLists)
        viewState.showSelectionMode(selectedPlaylists)
        viewState.setItemsSelected(true)
    }

    fun onPlayNextSelectedPlaylistsClicked() {
        addPlaylistsToPlayNext(ArrayList(selectedPlaylists))
        closeSelectionMode()
    }

    fun onAddToQueueSelectedPlaylistsClicked() {
        addPlaylistsToPlayQueue(ArrayList(selectedPlaylists))
        closeSelectionMode()
    }

    fun onAddSelectedPlaylistsToPlayListClicked() {
        viewState.showSelectPlayListDialog(selectedPlaylists, true)
    }

    fun onPlayListToAddingSelected(playList: PlayList, playlists: LongArray, closeMultiselect: Boolean) {
        performAddToPlaylist(
            playListsInteractor.getCompositionsByPlaylistsIds(playlists),
            playList
        ) { onAddingToPlayListCompleted(closeMultiselect) }
    }

    fun onExportSelectedPlaylistsClicked() {
        playlistsForExport.clear()
        playlistsForExport.addAll(selectedPlaylists)
        viewState.launchPickFolderScreen()
    }

    fun onShareSelectedPlaylistsClicked() {
        sharePlaylistsCompositions(ArrayList(selectedPlaylists))
    }

    fun onSelectionModeBackPressed() {
        closeSelectionMode()
    }

    fun onPlayPlaylistClicked(playlist: PlayList) {
        startPlaying(listOf(playlist))
    }

    fun onPlayNextPlaylistClicked(position: Int) {
        addPlaylistsToPlayNext(listOf(playLists[position]))
    }

    fun onPlayNextPlaylistClicked(playlist: PlayList) {
        addPlaylistsToPlayNext(listOf(playlist))
    }

    fun onAddToQueuePlaylistClicked(playlist: PlayList) {
        addPlaylistsToPlayQueue(listOf(playlist))
    }

    fun onAddPlaylistToPlayListClicked(playlist: PlayList) {
        viewState.showSelectPlayListDialog(listOf(playlist), false)
    }

    fun onSharePlaylistClicked(playlist: PlayList) {
        sharePlaylistsCompositions(listOf(playlist))
    }
    
    fun onDeletePlayListButtonClicked(playList: PlayList) {
        viewState.showConfirmDeletePlayListsDialog(listOf(playList))
    }

    fun onDeleteSelectedPlaylistsButtonClicked() {
        viewState.showConfirmDeletePlayListsDialog(selectedPlaylists)
    }

    fun onDeletePlayListDialogConfirmed(playLists: Collection<PlayList>) {
        playListsInteractor.deletePlayLists(playLists)
            .subscribe(
                { onDeletePlaylistsSuccess(playLists) },
                viewState::showDeletePlayListError
            )
    }

    fun onFragmentResumed() {
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

    fun onSearchTextChanged(text: String?) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text
            subscribeOnPlayLists()
        }
    }

    fun getSelectedPlaylists(): HashSet<PlayList> = selectedPlaylists

    fun getSearchText() = searchText

    private fun onDeletePlaylistsSuccess(playLists: Collection<PlayList>) {
        viewState.showPlayListsDeleteSuccess(playLists)
        if (selectedPlaylists.isNotEmpty()) {
            closeSelectionMode()
        }
    }

    private fun sharePlaylistsCompositions(playlists: List<PlayList>) {
        playListsInteractor.getCompositionsInPlaylists(playlists)
            .mapError(::ReceiveCompositionsForSendException)
            .launchOnUi(viewState::sendCompositions, viewState::showErrorMessage)
    }

    private fun onAddingToPlayListCompleted(closeMultiselect: Boolean) {
        if (closeMultiselect && selectedPlaylists.isNotEmpty()) {
            closeSelectionMode()
        }
    }
    
    private fun playSelectedPlaylists() {
        startPlaying(ArrayList(selectedPlaylists))
        closeSelectionMode()
    }

    private fun startPlaying(playlists: List<PlayList>) {
        playListsInteractor.startPlaying(playlists).runOnUi(viewState::showErrorMessage)
    }

    private fun addPlaylistsToPlayNext(playlists: List<PlayList>) {
        addCompositionsToPlayNext(playListsInteractor.getCompositionsInPlaylists(playlists))
    }

    private fun addPlaylistsToPlayQueue(playlists: List<PlayList>) {
        addCompositionsToEndOfQueue(playListsInteractor.getCompositionsInPlaylists(playlists))
    }

    private fun closeSelectionMode() {
        selectedPlaylists.clear()
        viewState.showSelectionMode(emptySet())
        viewState.setItemsSelected(false)
    }

    private fun importPlaylistFile(file: FileReference, overwriteExisting: Boolean) {
        playListsInteractor.importPlaylistFile(file, overwriteExisting)
            .justSubscribeOnUi(viewState::launchPlayListScreen, this::onPlaylistFileImportError)
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
        closeSelectionMode()
    }

    private fun subscribeOnPlayLists() {
        if (playLists.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(playlistsDisposable, presenterDisposable)
        playlistsDisposable = playListsInteractor.getPlayListsObservable(searchText)
            .observeOn(uiScheduler)
            .subscribe(this::onPlayListsReceived)
        presenterDisposable.add(playlistsDisposable!!)
    }

    private fun onPlayListsReceived(list: List<PlayList>) {
        val firstReceive = playLists.isEmpty()
        playLists = list
        viewState.updateList(list)
        if (list.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                viewState.showEmptyList()
            } else {
                viewState.showEmptySearchResult()
            }
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