package com.github.anrimian.musicplayer.ui.playlists.list


import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistAlreadyExistsException
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistNotCompletelyImportedException
import com.github.anrimian.musicplayer.data.models.folders.toFileReference
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.menu.AppMenu
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.coroutines.mapError
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.ConfirmDeletePlaylistDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.MenuConfigDialogData
import com.github.anrimian.musicplayer.ui.common.dialogs.share.models.ReceiveCompositionsForSendException
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.createDeletedPlaylistsMessage
import com.github.anrimian.musicplayer.ui.common.format.createExportedPlaylistsMessage
import com.github.anrimian.musicplayer.ui.common.lists.ScrollToPositionEffect
import com.github.anrimian.musicplayer.ui.common.menu.utils.MenuConfigUtil
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuDefinitions
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.common.models.menu.MenuIds
import com.github.anrimian.musicplayer.ui.common.mvvm.EmptyPersistent
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.toStatedData
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryViewModel
import com.github.anrimian.musicplayer.ui.playlists.create.CreatePlaylistDialogData
import com.github.anrimian.musicplayer.ui.playlists.list.PlaylistsDialogs.NotCompletelyImportedPlaylistDialog
import com.github.anrimian.musicplayer.ui.playlists.list.PlaylistsDialogs.OverwritePlaylistDialog
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlaylistDialogData
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow

class PlaylistsViewModel(
    private val playListsInteractor: PlaylistsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
): BaseLibraryViewModel<PlaylistsState, EmptyPersistent>(
    playerInteractor,
    playListsInteractor,
    PlaylistsState(),
    EmptyPersistent,
    savedStateHandle,
    errorParser
) {

    private var playlistsJob: Job? = null
    private var isFirstDataLoad = true

    private var playlistsForExport: List<Playlist> = emptyList()

    init {
        subscribeOnPlayLists()
        subscribeOnMenuConfig()
        observeSelectedItems()
        checkPlaylistToImportArg()
    }

    fun onTryAgainButtonClicked() {
        subscribeOnPlayLists()
    }

    fun onStop(listPosition: ListPosition) {
        playListsInteractor.saveListPosition(listPosition)
    }

    fun onPlaylistMenuItemClicked(menuItem: AppMenuItem, playlist: Playlist) {
        when (menuItem.id) {
            MenuIds.PLAY -> {
                startPlaying(listOf(playlist))
            }
            MenuIds.PLAY_NEXT -> {
                addPlaylistsToPlayNext(listOf(playlist))
            }
            MenuIds.ADD_TO_QUEUE -> {
                addPlaylistsToPlayQueue(listOf(playlist))
            }
            MenuIds.ADD_TO_PLAYLIST -> {
                sendEffect(PlaylistsEffect.ShowSelectPlayListDialog(
                    playlistIds = longArrayOf(playlist.id),
                    closeSelectionMode = false
                ))
            }
            MenuIds.EDIT_NAME -> {
                showDialog(RenamePlaylistDialogData(
                    playlistId = playlist.id,
                    initialName = playlist.name
                ))
            }
            MenuIds.EXPORT_PLAYLIST -> {
                playlistsForExport = listOf(playlist)
                sendEffect(PlaylistsEffect.LaunchPickFolder)
            }
            MenuIds.SHARE -> {
                sharePlaylistsCompositions(listOf(playlist))
            }
            MenuIds.DELETE -> {
                showDialog(ConfirmDeletePlaylistDialogData(
                    ids = longArrayOf(playlist.id),
                    singleName = playlist.name
                ))
            }
            MenuIds.MENU_CONFIG -> {
                showDialog(MenuConfigDialogData(AppMenu.PLAYLIST))
            }
        }
    }

    fun onCreatePlaylistButtonClicked() {
        showDialog(CreatePlaylistDialogData)
    }

    fun onCreatePlaylistDialogClosed() {
        dismissDialog()
    }

    fun onMenuConfigDialogClosed() {
        dismissDialog()
    }

    fun onPlayNextPlaylistClicked(playList: Playlist) {
        addPlaylistsToPlayNext(listOf(playList))
    }

    fun onPlaylistClicked(playList: Playlist) {
        val playListId = playList.id
        val selectedPlaylists = currentState.selectedPlaylists
        if (selectedPlaylists.isEmpty()) {
            sendNavigationEffect(Screen.PlaylistDetails(playListId))
            return
        }
        updateState {
            val newSelectedPlaylists = if (selectedPlaylists.contains(playListId)) {
                selectedPlaylists.remove(playListId)
            } else {
                selectedPlaylists.add(playListId)
            }
            copy(selectedPlaylists = newSelectedPlaylists)
        }
    }

    fun onPlaylistLongClicked(playList: Playlist) {
        updateState { copy(selectedPlaylists = selectedPlaylists.add(playList.id)) }
    }

    fun onPlayAllSelectedClicked() {
        startPlaying(getSelectedPlaylists())
        closeSelectionMode()
    }

    fun onSelectAllButtonClicked() {
        val playlists = currentState.playlists.data ?: return
        updateState { copy(selectedPlaylists = playlists.map(Playlist::id).toPersistentSet()) }
    }

    fun onPlayNextSelectedPlaylistsClicked() {
        addPlaylistsToPlayNext(getSelectedPlaylists())
        closeSelectionMode()
    }

    fun onAddToQueueSelectedPlaylistsClicked() {
        addPlaylistsToPlayQueue(getSelectedPlaylists())
        closeSelectionMode()
    }

    fun onAddSelectedPlaylistsToPlayListClicked() {
        val selectedIds = currentState.selectedPlaylists.toList().toLongArray()
        if (selectedIds.isNotEmpty()) {
            sendEffect(PlaylistsEffect.ShowSelectPlayListDialog(
                playlistIds = selectedIds,
                closeSelectionMode = true
            ))
        }
    }

    fun onPlayListToAddingSelected(
        targetPlaylist: Playlist,
        sourcePlaylistIds: LongArray,
        closeSelectionMode: Boolean,
    ) {
        performAddToPlaylist(
            compositionsFetcher = {
                playListsInteractor.getCompositionsByPlaylistsIds(sourcePlaylistIds)
            },
            playList = targetPlaylist,
            onComplete = {
                if (closeSelectionMode) {
                    closeSelectionMode()
                }
            }
        )
    }

    fun onExportSelectedPlaylistsClicked() {
        playlistsForExport = getSelectedPlaylists()
        if (playlistsForExport.isNotEmpty()) {
            sendEffect(PlaylistsEffect.LaunchPickFolder)
        }
    }

    fun onShareSelectedPlaylistsClicked() {
        sharePlaylistsCompositions(getSelectedPlaylists())
    }

    fun onExitSelectionModeClicked() {
        closeSelectionMode()
    }

    fun onDeleteSelectedPlaylistsButtonClicked() {
        val ids = currentState.selectedPlaylists.toList().toLongArray()
        showDialog(ConfirmDeletePlaylistDialogData(ids = ids))
    }

    fun onConfirmDeletePlaylistsDialogConfirmed() {
        withCurrentDialog<ConfirmDeletePlaylistDialogData> { data ->
            dismissDialog()
            launch(onError = { e ->
                sendMessage(UiText.StringResource(R.string.play_list_delete_error, e.message))
            }) {
                playListsInteractor.deletePlaylists(data.ids)
                updateState { copy(selectedPlaylists = selectedPlaylists.removeAll(data.ids.toSet())) }
                sendMessage(createDeletedPlaylistsMessage(data.ids, data.singleName))
            }
        }
    }

    fun onConfirmDeletePlaylistsDialogClosed() {
        dismissDialog()
    }

    fun onFragmentResumed() {
        playListsInteractor.setSelectedPlaylistScreen(0)
    }

    fun onRenamePlaylistDialogClosed() {
        dismissDialog()
    }

    fun onFolderForExportSelected(folder: FileReference) {
        val playlistsToExport = ArrayList(playlistsForExport)
        playlistsForExport = emptyList()
        if (playlistsToExport.isEmpty()) {
            return
        }

        launch(onError = ::sendErrorMessage) {
            playListsInteractor.exportPlaylistsToFolder(playlistsToExport, folder)
            sendMessage(createExportedPlaylistsMessage(playlistsToExport))
            closeSelectionMode()
        }
    }

    fun onPlaylistFileReceived(file: FileReference) {
        importPlaylistFile(file, false)
    }

    fun onOverwritePlaylistDialogClosed() {
        dismissDialog()
    }

    fun onOverwritePlaylistDialogConfirmed() {
        withCurrentDialog<OverwritePlaylistDialog> { dialogData ->
            dismissDialog()
            val fileRef = dialogData.filePayload.toFileReference()
            importPlaylistFile(fileRef, overwriteExisting = true)
        }
    }

    fun onNotCompletelyImportedPlaylistDialogClosed() {
        dismissDialog()
    }

    fun onNotCompletelyImportedPlaylistDialogConfirmed() {
        withCurrentDialog<NotCompletelyImportedPlaylistDialog> { dialogData ->
            dismissDialog()
            sendNavigationEffect(Screen.PlaylistDetails(dialogData.playlistId))
        }
    }

    fun onSearchTextChanged(text: String?) {
        if (currentState.searchQuery == text) {
            return
        }
        updateState { copy(searchQuery = text) }
    }

    fun getSearchText() = currentState.searchQuery

    private fun sharePlaylistsCompositions(playlists: List<Playlist>) {
        launch(onError = ::sendErrorMessage) {
            mapError(::ReceiveCompositionsForSendException) {
                val compositions = playListsInteractor.getCompositionsInPlaylists(playlists)
                shareCompositions(compositions)
            }
        }
    }

    private fun startPlaying(playlists: List<Playlist>) {
        launch(onError = ::sendErrorMessage) {
            playListsInteractor.startPlaying(playlists)
        }
    }

    private fun addPlaylistsToPlayNext(playlists: List<Playlist>) {
        addCompositionsToPlayNext { playListsInteractor.getCompositionsInPlaylists(playlists) }
    }

    private fun addPlaylistsToPlayQueue(playlists: List<Playlist>) {
        addCompositionsToEndOfQueue { playListsInteractor.getCompositionsInPlaylists(playlists) }
    }

    private fun closeSelectionMode() {
        updateState { copy(selectedPlaylists = persistentSetOf()) }
    }

    private fun importPlaylistFile(file: FileReference, overwriteExisting: Boolean) {
        launchCatching(onError = { t -> this.onPlaylistFileImportError(t, file) }) {
            val playlistId = playListsInteractor.importPlaylistFile(file, overwriteExisting)
            sendNavigationEffect(Screen.PlaylistDetails(playlistId))
        }
    }

    private fun onPlaylistFileImportError(throwable: Throwable, file: FileReference) {
        when (throwable) {
            is PlaylistAlreadyExistsException -> showDialog(OverwritePlaylistDialog(file.path))
            is PlaylistNotCompletelyImportedException -> {
                showDialog(NotCompletelyImportedPlaylistDialog(
                    throwable.playlistId,
                    throwable.notFoundFilesCount
                ))
            }
            else -> sendErrorMessage(throwable)
        }
    }

    private fun subscribeOnPlayLists() {
        playlistsJob?.cancel()
        playlistsJob = state
            .map { state -> state.searchQuery }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                playListsInteractor.getPlaylistsObservable(query).asFlow()
                    .map { list ->
                        val emptyMessageResId = if (query.isNullOrEmpty()) {
                            R.string.play_lists_on_device_not_found
                        } else {
                            R.string.no_matching_search_results_found
                        }
                        list.toImmutableList().toStatedData(emptyMessageResId)
                    }
            }
            .subscribeStated { state ->
                updateState { copy(playlists = state) }
                if (state is StatedData.Content && isFirstDataLoad) {
                    isFirstDataLoad = false
                    val listPosition = playListsInteractor.getSavedListPosition()
                    if (listPosition != null) {
                        sendEffect(ScrollToPositionEffect(listPosition))
                    }
                }
            }
    }

    private fun subscribeOnMenuConfig() {
        playListsInteractor.getPlaylistMenuConfigFlow()
            .subscribe { config ->
                val items = MenuConfigUtil.applyConfig(AppMenuDefinitions.PlaylistMenuItems, config)
                updateState { copy(menuItems = items.toImmutableList()) }
            }
    }

    private fun observeSelectedItems() {
        state.map { state -> state.selectedPlaylists to state.playlists }
            .distinctUntilChanged()
            .map { (selectedIds, playlistsState) ->
                if (selectedIds.isEmpty()) {
                    return@map null
                }
                val allData = playlistsState.data ?: return@map null

                val totalCompositions = allData.sumOf { playlist ->
                    if (selectedIds.contains(playlist.id)) playlist.compositionsCount else 0
                }

                SelectionModeState(selectedIds.size, totalCompositions)
            }
            .distinctUntilChanged()
            .subscribe { modeState -> updateState { copy(selectionModeState = modeState) } }
    }

    private fun getSelectedPlaylists(): List<Playlist> {
        val playlists = currentState.playlists.data ?: return emptyList()
        val selectedIds = currentState.selectedPlaylists.toList()
        val playlistIdMap = playlists.associateBy(Playlist::id)
        return selectedIds.mapNotNull { id -> playlistIdMap[id] }
    }

    private fun checkPlaylistToImportArg() {
        val playlistFileToImport = getAndClearArg<String>(AppConstants.Arguments.PLAYLIST_IMPORT_ARG)
        if (playlistFileToImport != null) {
            importPlaylistFile(playlistFileToImport.toFileReference(), false)
        }
    }

}