package com.github.anrimian.musicplayer.ui.player_screen

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.fsync.models.Optional
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.screen.ActionState
import com.github.anrimian.musicplayer.domain.interactors.player.screen.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import java.util.LinkedList

/**
 * Created on 02.11.2017.
 */
class PlayerPresenter(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playerScreenInteractor: PlayerScreenInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    playListsInteractor: PlaylistsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler,
) : BaseLibraryPresenter<PlayerView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private var currentItem: PlayQueueItem? = null

    private var isCoversEnabled = false

    private val compositionsForPlayList = LinkedList<Composition>()

    private var lastDeleteAction: Completable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.setButtonPanelState(playerScreenInteractor.isPlayerPanelOpen())
        viewState.showActionState(ActionState.NO_STATE)
        subscribeOnUiSettings()
        subscribeOnRandomMode()
        subscribeOnSpeedAvailableState()
        subscribeOnSpeedState()
        subscribeOnRepeatMode()
        subscribeOnPlayerStateChanges()
        subscribeOnErrorEvents()
        subscribeOnCurrentComposition()
        subscribeOnCurrentCompositionSyncState()
        subscribeOnTrackPositionChanging()
        subscribeOnSleepTimerTime()
        subscribeOnCurrentActionsState()
        playerScreenInteractor.getPlayerScreensSwipeObservable()
            .unsafeSubscribeOnUi(viewState::showScreensSwipeEnabled)
        playerScreenInteractor.getVolumeObservable()
            .map(VolumeState::toLong)
            .unsafeSubscribeOnUi(viewState::onVolumeChanged)
        playerScreenInteractor.getActionStateObservable()
            .unsafeSubscribeOnUi(viewState::showActionState)

        syncInteractor.start()
    }

    fun onSetupScreenStateRequested() {
        viewState.showDrawerScreen(
            playerScreenInteractor.getSelectedDrawerScreen(),
            playerScreenInteractor.getSelectedPlayListScreenId()
        )
        viewState.showPlayerContentPage(playerScreenInteractor.getPlayerContentPage())
    }

    fun onOpenPlayerPanelClicked() {
        playerScreenInteractor.setPlayerPanelOpen(true)
    }

    fun onBottomPanelExpanded() {
        playerScreenInteractor.setPlayerPanelOpen(true)
        viewState.setButtonPanelState(true)
    }

    fun onBottomPanelCollapsed() {
        playerScreenInteractor.setPlayerPanelOpen(false)
        viewState.setButtonPanelState(false)
    }

    fun onDrawerScreenSelected(screenId: Int) {
        playerScreenInteractor.setSelectedDrawerScreen(screenId)
        viewState.showDrawerScreen(screenId, 0)
    }

    fun onLibraryScreenSelected() {
        viewState.showLibraryScreen(
            playerScreenInteractor.getSelectedLibraryScreen(),
            playerScreenInteractor.getSelectedArtistScreenId(),
            playerScreenInteractor.getSelectedAlbumScreenId(),
            playerScreenInteractor.getSelectedGenreScreenId(),
        )
    }

    fun onLibraryScreenSelected(screenId: Int) {
        playerScreenInteractor.setSelectedLibraryScreen(screenId)
    }

    fun onPlayerContentPageChanged(position: Int) {
        playerScreenInteractor.setPlayerContentPage(position)
    }

    fun onPlayButtonClicked() {
        playerInteractor.play()
    }

    fun onStopButtonClicked() {
        playerInteractor.pause()
    }

    fun onSkipToPreviousButtonClicked() {
        playerInteractor.skipToPrevious()
    }

    fun onSkipToNextButtonClicked() {
        playerInteractor.skipToNext()
    }

    fun onRepeatModeChanged(mode: Int) {
        playerInteractor.setRepeatMode(mode)
    }

    fun onChangeRandomModeClicked() {
        playerInteractor.changeRandomMode()
    }

    fun onTrackRewoundTo(progress: Int) {
        playerInteractor.seekTo(progress.toLong())
    }

    fun onDeleteCompositionButtonClicked(composition: Composition) {
        viewState.showConfirmDeleteDialog(listOf(composition))
    }

    fun onAddQueueItemToPlayListButtonClicked(composition: Composition) {
        compositionsForPlayList.clear()
        compositionsForPlayList.add(composition)
        viewState.showSelectPlayListDialog()
    }

    fun onPlayListForAddingSelected(playList: Playlist) {
        addPreparedCompositionsToPlayList(playList)
    }

    fun onDeleteCompositionsDialogConfirmed(compositionsToDelete: List<Composition>) {
        deletePreparedCompositions(compositionsToDelete)
    }

    fun onSeekStart() {
        playerInteractor.onSeekStarted()
    }

    fun onSeekStop(progress: Int) {
        playerInteractor.onSeekFinished(progress.toLong())
    }

    fun onRestoreDeletedItemClicked() {
        playerInteractor.restoreDeletedItem().justRunOnUi(viewState::showErrorMessage)
    }

    fun onFastSeekForwardCalled() {
        playerInteractor.fastSeekForward()
    }

    fun onFastSeekBackwardCalled() {
        playerInteractor.fastSeekBackward()
    }

    fun onRetryFailedDeleteActionClicked() {
        if (lastDeleteAction != null) {
            lastDeleteAction!!
                .doFinally { lastDeleteAction = null }
                .justSubscribe(this::onDeleteCompositionError)
        }
    }

    fun onPlaybackSpeedSelected(speed: Float) {
        viewState.showPlaybackSpeed(speed)
        playerInteractor.setPlaybackSpeed(speed)
    }

    fun getPlayerContentPage() = playerScreenInteractor.getPlayerContentPage()

    fun isPlayerPanelOpened() = playerScreenInteractor.isPlayerPanelOpen()

    private fun subscribeOnRepeatMode() {
        playerInteractor.getRepeatModeObservable().unsafeSubscribeOnUi(viewState::showRepeatMode)
    }

    private fun addPreparedCompositionsToPlayList(playList: Playlist) {
        performAddToPlaylist(compositionsForPlayList, playList) { compositionsForPlayList.clear() }
    }

    private fun deletePreparedCompositions(compositionsToDelete: List<Composition>) {
        lastDeleteAction = playerInteractor.deleteCompositions(compositionsToDelete)
            .observeOn(uiScheduler)
            .doOnSuccess(viewState::showDeleteCompositionMessage)
            .ignoreElement()
        lastDeleteAction!!.justSubscribe(this::onDeleteCompositionError)
    }

    private fun onDeleteCompositionError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun subscribeOnCurrentCompositionSyncState() {
        playerScreenInteractor.getCurrentCompositionFileSyncState()
            .unsafeSubscribeOnUi(this::onCurrentCompositionSyncStateReceived)
    }

    private fun onCurrentCompositionSyncStateReceived(fileSyncStateOpt: Optional<FileSyncState>) {
        viewState.showCurrentCompositionSyncState(fileSyncStateOpt.value, currentItem)
    }

    private fun subscribeOnCurrentComposition() {
        playerInteractor.getCurrentQueueItemObservable()
            .unsafeSubscribeOnUi(this::onPlayQueueEventReceived)
    }

    private fun onPlayQueueEventReceived(playQueueEvent: PlayQueueEvent) {
        val newItem = playQueueEvent.playQueueItem
        val currentItem = this.currentItem

        if (currentItem == null
            || currentItem != newItem
            || !CompositionHelper.areSourcesTheSame(newItem, currentItem)) {

            var updateCover = false
            if ((currentItem == null) != (newItem == null)) {
                updateCover = true
            } else if (currentItem != null && newItem != null)  {
                updateCover = currentItem.modifiedTime != newItem.modifiedTime
                        || currentItem.coverModifyTime != newItem.coverModifyTime
                        || currentItem.size != newItem.size
                        || currentItem.isFileExists != newItem.isFileExists
            }

            this.currentItem = newItem
            viewState.showCurrentQueueItem(newItem)

            if (updateCover) {
                showCurrentItemCover(newItem)
            }
        }
    }

    private fun subscribeOnPlayerStateChanges() {
        playerInteractor.getIsPlayingStateObservable()
            .unsafeSubscribeOnUi(viewState::showPlayingState)
    }

    private fun subscribeOnErrorEvents() {
        playerInteractor.getPlayerStateObservable()
            .unsafeSubscribeOnUi(this::onPlayerStateReceived)
    }

    private fun onPlayerStateReceived(playerState: PlayerState) {
        if (playerState is PlayerState.Error) {
            if (playerState.throwable is UnavailableMediaStoreException) {
                //after lazy-prepare implementation this case can be removed.
                // Do not forget to check after remove.
                viewState.showPlayErrorState(null)
            } else {
                val errorCommand = errorParser.parseError(playerState.throwable)
                viewState.showPlayErrorState(errorCommand)
            }
        } else {
            viewState.showPlayErrorState(null)
        }
    }

    private fun subscribeOnTrackPositionChanging() {
        playerInteractor.getTrackPositionObservable()
            .unsafeSubscribeOnUi(this::onTrackPositionChanged)
    }

    private fun onTrackPositionChanged(currentPosition: Long) {
        currentItem?.let { item -> viewState.showTrackState(currentPosition, item.duration) }
    }

    private fun subscribeOnUiSettings() {
        playerScreenInteractor.getCoversEnabledObservable()
            .subscribeOnUi(this::onUiSettingsReceived, errorParser::logError)
    }

    private fun onUiSettingsReceived(isCoversEnabled: Boolean) {
        this.isCoversEnabled = isCoversEnabled
        showCurrentItemCover(currentItem)
    }

    private fun showCurrentItemCover(item: PlayQueueItem?) {
        val currentItem = if (isCoversEnabled) item else null
        viewState.showCurrentItemCover(currentItem)
    }

    private fun subscribeOnRandomMode() {
        playerInteractor.getRandomPlayingObservable()
            .unsafeSubscribeOnUi(viewState::showRandomMode)
    }

    private fun subscribeOnSpeedAvailableState() {
        playerInteractor.getSpeedChangeAvailableObservable()
            .unsafeSubscribeOnUi(viewState::showSpeedChangeFeatureVisible)
    }

    private fun subscribeOnSpeedState() {
        playerInteractor.getPlaybackSpeedObservable()
            .unsafeSubscribeOnUi(viewState::showPlaybackSpeed)
    }

    private fun subscribeOnSleepTimerTime() {
        playerScreenInteractor.getSleepTimerCountDownObservable()
            .unsafeSubscribeOnUi(viewState::showSleepTimerRemainingTime)
    }

    private fun subscribeOnCurrentActionsState() {
        playerScreenInteractor.getCurrentActionsObservable()
            .runOnUi(viewState::showCurrentActions, viewState::showErrorMessage)
    }

}