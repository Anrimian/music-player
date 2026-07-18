package com.github.anrimian.musicplayer.ui.playlists.details

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.menu.AppMenu
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.ConfirmDeletePlaylistDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.compositions.ConfirmDeleteCompositionsDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.MenuConfigDialogData
import com.github.anrimian.musicplayer.ui.common.delete.FileDeletionHandler
import com.github.anrimian.musicplayer.ui.common.effects.MessageDuration
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.createDeletePlaylistItemCompleteMessage
import com.github.anrimian.musicplayer.ui.common.format.createExportedPlaylistsMessage
import com.github.anrimian.musicplayer.ui.common.lists.ScrollToPositionEffect
import com.github.anrimian.musicplayer.ui.common.menu.utils.MenuConfigUtil
import com.github.anrimian.musicplayer.ui.common.models.fsync.toUiStateMap
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuDefinitions
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.common.models.menu.MenuIds
import com.github.anrimian.musicplayer.ui.common.mvvm.EmptyPersistent
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.toStatedData
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryViewModel
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogData
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlaylistDialogData
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ListDragFilter
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class PlaylistDetailsViewModel(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlaylistsInteractor,
    displaySettingsInteractor: DisplaySettingsInteractor,
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    private val fileDeletionHandler: FileDeletionHandler,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : BaseLibraryViewModel<PlaylistDetailsState, EmptyPersistent>(
    playerInteractor,
    playListsInteractor,
    PlaylistDetailsState(),
    EmptyPersistent,
    savedStateHandle,
    errorParser
) {

    private val args = getArgs<Screen.PlaylistDetails>()
    private val playListId = args.playlistId

    private var playlistEntriesJob: Job? = null
    private var isFirstDataLoad = true

    private val listDragFilter = ListDragFilter()

    init {
        subscribePlaylist()
        subscribeOnPlaylistEntries()
        subscribeOnMenuConfig()

        syncInteractor.getFilesSyncStateObservable().asFlow()
            .subscribe { stateMap ->
                updateState { copy(fileSyncStates = stateMap.toUiStateMap()) }
            }

        playerInteractor.getCurrentCompositionObservable().asFlow()
            .subscribeCatching(
                onNext = { composition ->
                    updateState { copy(currentComposition = composition) }
                },
                onError = errorParser::logError
            )

        playerInteractor.getRandomPlayingObservable().asFlow()
            .subscribeCatching(
                onNext = { randomMode -> updateState { copy(isRandomEnabled = randomMode) } },
                onError = errorParser::logError
            )

        displaySettingsInteractor.getCoversEnabledObservable().asFlow()
            .subscribe { coversEnabled ->
                updateState { copy(isCoversEnabled = coversEnabled) }
            }
    }

    fun onTryAgainButtonClicked() {
        subscribeOnPlaylistEntries()
    }

    fun onStop(listPosition: ListPosition) {
        playListsInteractor.saveItemsListPosition(playListId, listPosition)
    }

    fun onItemClicked(item: PlaylistEntry, position: Int) {
        if (item.id == currentState.currentComposition?.id) {
            playerInteractor.playOrPause()
        } else {
            startPlaying(position)
            // quick update - start playing is long(re-inserts queue)
            updateState { copy(currentComposition = CurrentComposition(item, true)) }
        }
    }

    fun onPlaylistEntryMenuItemClicked(
        menuItem: AppMenuItem,
        item: PlaylistEntry,
        position: Int,
    ) {
        when (menuItem.id) {
            MenuIds.PLAY -> {
                startPlaying(position)
            }
            MenuIds.PLAY_NEXT -> {
                addCompositionsToPlayNext(listOf(item))
            }
            MenuIds.ADD_TO_QUEUE -> {
                addCompositionsToEndOfQueue(listOf(item))
            }
            MenuIds.ADD_TO_PLAYLIST -> {
                sendEffect(PlaylistDetailsEffect.ShowSelectPlaylistDialog(longArrayOf(item.id)))
            }
            MenuIds.EDIT_TAGS -> {
                navigateTo(Screen.TagsEditor(item.id))
            }
            MenuIds.SHOW_IN_FOLDERS -> {
                sendEffect(PlaylistDetailsEffect.ShowInFolders(item.id))
            }
            MenuIds.SHARE -> {
                shareCompositions(listOf(item))
            }
            MenuIds.DELETE_FROM_PLAYLIST -> {
                deleteItem(item)
            }
            MenuIds.DELETE -> {
                showDialog(ConfirmDeleteCompositionsDialogData(listOf(item)))
            }
            MenuIds.MENU_CONFIG -> {
                showDialog(MenuConfigDialogData(AppMenu.PLAYLIST_ENTRY))
            }
        }
    }

    fun onMenuConfigDialogClosed() {
        dismissDialog()
    }

    fun onConfirmDeleteCompositionsDialogConfirmed() {
        withCurrentDialog<ConfirmDeleteCompositionsDialogData> { dialog ->
            dismissDialog()
            launch(onError = { e -> fileDeletionHandler.handleError(e, ::sendEffect) }) {
                fileDeletionHandler.performFilesDelete(
                    compositionsFetcher = { dialog.ids.locateCompositions() },
                    outputEffects = ::sendEffect
                )
            }
        }
    }

    fun onConfirmDeleteCompositionsDialogDismissed() {
        dismissDialog()
    }

    fun onDeletePermissionResult(isGranted: Boolean) {
        launch(onError = { e -> fileDeletionHandler.handleError(e, ::sendEffect) }) {
            fileDeletionHandler.onPermissionResult(
                isGranted = isGranted,
                outputEffects = ::sendEffect
            )
        }
    }

    fun onPlaylistToAddingSelected(targetPlaylist: Playlist, sourceCompositionIds: LongArray) {
        performAddToPlaylist(
            compositionsFetcher = { sourceCompositionIds.locateCompositions() },
            playList = targetPlaylist
        )
    }

    fun onDeletePlaylistButtonClicked() {
        val playList = currentState.playlist ?: return
        showDialog(ConfirmDeletePlaylistDialogData(
            ids = longArrayOf(playListId),
            singleName = playList.name
        ))
    }

    fun onDeletePlaylistDialogConfirmed() {
        withCurrentDialog<ConfirmDeletePlaylistDialogData> {
            dismissDialog()
            launch(onError = { e ->
                sendMessage(UiText.StringResource(R.string.play_list_delete_error, e.message))
            }) {
                playListsInteractor.deletePlaylist(playListId)
            }
        }
    }

    fun onConfirmDeletePlaylistDialogClosed() {
        dismissDialog()
    }

    fun onFragmentResumed() {
        playListsInteractor.setSelectedPlaylistScreen(playListId)
    }

    fun onItemSwipedToPlayNext(item: PlaylistEntry) {
        addCompositionsToPlayNext(listOf(item))
    }

    fun onItemSwipedToDelete(item: PlaylistEntry) {
        deleteItem(item)
    }

    fun onItemMoved(from: Int, to: Int) {
        val currentData = currentState.playlistItems
        if (currentData is StatedData.Content) {
            val oldList = currentData.data
            val newList = oldList.mutate { list ->
                list.apply { add(to, removeAt(from)) }
            }
            updateState {
                copy(playlistItems = currentData.copy(data = newList))
            }
        }
    }

    fun onItemDragEnded(from: Int, to: Int) {
        listDragFilter.increaseEventsToSkip()
        launch(onError = ::sendErrorMessage) {
            playListsInteractor.moveItemInPlaylist(playListId, from, to).await()
        }
    }

    fun onRestoreRemovedEntryClicked() {
        launch(onError = ::sendErrorMessage) {
            playListsInteractor.restoreDeletedPlaylistItem().await()
        }
    }

    fun onChangePlaylistNameButtonClicked() {
        val playlist = currentState.playlist ?: return

        showDialog(RenamePlaylistDialogData(
            playlistId = playlist.id,
            initialName = playlist.name
        ))
    }

    fun onRenamePlaylistDialogClosed() {
        dismissDialog()
    }

    fun onPlayAllButtonClicked() {
        startPlaying()
    }

    fun onChangeRandomModeClicked() {
        playerInteractor.changeRandomMode()
    }

    fun onSortButtonClicked() {
        showDialog(SelectOrderDialogData(
            allowedOrderTypes = listOf(OrderType.NAME, OrderType.ARTIST, OrderType.DURATION)
        ))
    }

    fun onSortSelected(order: Order) {
        dismissDialog()
        launch(onError = ::sendErrorMessage) {
            playListsInteractor.sortPlaylistEntries(playListId, order)
            sendMessage(
                message = UiText.StringResource(R.string.playlist_sorted),
                actionLabel = UiText.StringResource(R.string.undo),
                action = UndoSortAction,
                duration = MessageDuration.Custom(18_000L)
            )
        }
    }

    fun onSortDialogDismissed() {
        dismissDialog()
    }

    fun onUndoSortButtonClicked() {
        launch(onError = ::sendErrorMessage) {
            playListsInteractor.undoSortPlaylistEntries()
        }
    }

    fun onExportPlaylistClicked() {
        sendEffect(PlaylistDetailsEffect.LaunchPickFolder)
    }

    fun onFolderForExportSelected(folder: FileReference) {
        val playlist = currentState.playlist ?: return
        val playlistsToExport = listOf(playlist)

        launch(onError = ::sendErrorMessage) {
            playListsInteractor.exportPlaylistsToFolder(playlistsToExport, folder)
            sendMessage(createExportedPlaylistsMessage(playlistsToExport))
        }
    }

    fun onSearchTextChanged(text: String?) {
        if (currentState.searchQuery == text) {
            return
        }
        updateState { copy(searchQuery = text) }
    }

    fun getSearchText() = currentState.searchQuery

    private fun deleteItem(playListEntry: PlaylistEntry) {
        launch(onError = ::sendErrorMessage) {
            playListsInteractor.deleteItemFromPlaylist(playListEntry, playListId).await()
            val playlist = currentState.playlist ?: return@launch
            sendMessage(
                message = createDeletePlaylistItemCompleteMessage(playlist, listOf(playListEntry)),
                actionLabel = UiText.StringResource(R.string.cancel),
                action = RestoreRemovedPlaylistEntry
            )
        }
    }

    private fun subscribeOnPlaylistEntries() {
        playlistEntriesJob?.cancel()
        playlistEntriesJob = state
            .map { state -> state.searchQuery }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                playListsInteractor.getCompositionsObservable(playListId, query).asFlow()
                    .filter(listDragFilter::isEmitAllowed)
                    .map { list ->
                        val emptyMessageResId = if (query.isNullOrEmpty()) {
                            R.string.play_list_is_empty
                        } else {
                            R.string.no_matching_search_results_found
                        }
                        list.toPersistentList().toStatedData(emptyMessageResId)
                    }
            }
            .subscribeStated(onComplete = ::closeScreen) { state ->
                updateState { copy(playlistItems = state) }
                if (state is StatedData.Content && isFirstDataLoad) {
                    isFirstDataLoad = false
                    val listPosition = playListsInteractor.getSavedItemsListPosition(playListId)
                    if (listPosition != null) {
                        sendEffect(ScrollToPositionEffect(listPosition))
                    }
                }
            }
    }

    private fun subscribePlaylist() {
        playListsInteractor.getPlaylistFlow(playListId)
            .subscribe(
                onNext = { playlist -> updateState { copy(playlist = playlist) } },
                onComplete = ::closeScreen
            )
    }

    private fun subscribeOnMenuConfig() {
        playListsInteractor.getPlaylistEntryMenuConfigFlow()
            .subscribe { config ->
                val items = MenuConfigUtil.applyConfig(AppMenuDefinitions.PlaylistEntryMenuItems, config)
                updateState { copy(menuItems = items.toImmutableList()) }
            }
    }

    private fun startPlaying(position: Int = Constants.NO_POSITION) {
        launch(onError = ::sendErrorMessage) {
            val items = currentState.playlistItems.data ?: return@launch
            playerInteractor.setQueueAndPlay(items.map(PlaylistEntry::id), position).await()
        }
    }

    private fun LongArray.locateCompositions(): List<CompositionModel> {
        val currentItems = currentState.playlistItems.data ?: return emptyList()
        val sourceIdsSet = this.toSet()
        return currentItems.filter { playlistEntry -> playlistEntry.id in sourceIdsSet }
    }

}