package com.github.anrimian.musicplayer.ui.library.folders.volumes

import com.github.anrimian.musicplayer.data.utils.rx.mapError
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.share.models.ReceiveCompositionsForSendException
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import java.util.LinkedList

class LibraryVolumesPresenter(
    private val interactor: LibraryFoldersScreenInteractor,
    private val playerInteractor: LibraryPlayerInteractor,
    playListsInteractor: PlaylistsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : BaseLibraryPresenter<LibraryVolumesView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private var volumesDisposable: Disposable? = null

    private var volumeList: List<Volume> = ArrayList()
    private val volumesForPlayList: MutableList<Volume> = LinkedList()
    private val selectedVolumes = LinkedHashSet<Volume>()
    private var recentlyAddedIgnoredFolder: IgnoredFolder? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnVolumes()
        playerInteractor.getRandomPlayingObservable()
            .subscribeOnUi(viewState::showRandomMode, errorParser::logError)
    }

    fun onFragmentDisplayed() {
        interactor.saveCurrentFolder(null)
    }

    fun onRetryClicked() {
        subscribeOnVolumes()
    }

    fun onPlayVolumeClicked(volume: Volume) {
        startPlaying(listOf(volume))
    }

    fun onPlayNextVolumeClicked(position: Int) {
        val volume = volumeList.elementAtOrNull(position) ?: return
        onPlayNextVolumeClicked(volume)
    }

    fun onPlayNextVolumeClicked(volume: Volume) {
        addCompositionsToPlayNext(interactor.getAllCompositionsInFolder(volume.rootFolderId))
    }

    fun onAddToQueueVolumeClicked(volume: Volume) {
        addCompositionsToEndOfQueue(interactor.getAllCompositionsInFolder(volume.rootFolderId))
    }

    fun onPlayAllButtonClicked() {
        if (selectedVolumes.isEmpty()) {
            interactor.playAllMusicInFolder(null).runOnUi(viewState::showErrorMessage)
        } else {
            playSelectedVolumes()
        }
    }

    fun onAddVolumeToPlayListButtonClicked(volume: Volume) {
        viewState.showSelectPlayListFromVolumeDialog(volume)
    }

    fun onPlayListToAddingSelected(playList: Playlist) {
        addPreparedVolumesToPlayList(playList)
    }

    fun onPlayListSelected(folderId: Long, playList: Playlist) {
        performAddToPlaylist(interactor.getAllCompositionsInFolder(folderId), playList) {}
    }

    fun onShareVolumeClicked(volume: Volume) {
        shareFileSources(listOf(volume))
    }

    fun onVolumeClicked(position: Int, volume: Volume) {
        processMultiSelectClick(position, volume) { viewState.goToFolderScreen(volume.rootFolderId) }
    }

    fun onVolumeLongClicked(position: Int, volume: Volume) {
        selectedVolumes.add(volume)
        viewState.showSelectionMode(selectedVolumes.size)
        viewState.onItemSelected(volume, position)
    }

    fun onExitSelectionModeClicked() {
        closeSelectionMode()
    }

    fun onPlayAllSelectedClicked() {
        playSelectedVolumes()
    }

    fun onSelectAllButtonClicked() {
        selectedVolumes.clear() //reselect previous feature
        selectedVolumes.addAll(volumeList)
        viewState.showSelectionMode(volumeList.size)
        viewState.setItemsSelected(true)
    }

    fun onPlayNextSelectedSourcesClicked() {
        addCompositionsToPlayNext(interactor.getAllCompositionsInFileSources(ArrayList(selectedVolumes)))
        closeSelectionMode()
    }

    fun onAddToQueueSelectedSourcesClicked() {
        addCompositionsToEndOfQueue(interactor.getAllCompositionsInFileSources(ArrayList(selectedVolumes)))
        closeSelectionMode()
    }

    fun onAddSelectedSourcesToPlayListClicked() {
        volumesForPlayList.clear()
        volumesForPlayList.addAll(selectedVolumes)
        viewState.showSelectPlayListDialog()
    }

    fun onShareSelectedSourcesClicked() {
        shareFileSources(ArrayList(selectedVolumes))
    }

    fun onExcludeFolderClicked(volume: Volume) {
        interactor.addFolderToIgnore(volume)
            .launchOnUi(this::onIgnoreFolderAdded, viewState::showErrorMessage)
    }

    fun onRemoveIgnoredFolderClicked() {
        recentlyAddedIgnoredFolder?.let { folder ->
            launch(onError = viewState::showErrorMessage) { interactor.deleteIgnoredFolder(folder) }
        }
    }

    fun onChangeRandomModePressed() {
        playerInteractor.changeRandomMode()
    }

    fun getSelectedVolumes() = selectedVolumes

    private fun playSelectedVolumes() {
        startPlaying(selectedVolumes)
        closeSelectionMode()
    }

    private fun startPlaying(sources: Collection<Volume>, position: Int = Constants.NO_POSITION) {
        interactor.play(sources, position).runOnUi(viewState::showErrorMessage)
    }

    private fun onIgnoreFolderAdded(folder: IgnoredFolder) {
        recentlyAddedIgnoredFolder = folder
        viewState.showAddedIgnoredFolderMessage(folder)
    }

    private fun shareFileSources(volumes: List<Volume>) {
        interactor.getAllCompositionsInFileSources(volumes)
            .mapError(::ReceiveCompositionsForSendException)
            .launchOnUi(viewState::sendCompositions, viewState::showErrorMessage)
    }

    private fun processMultiSelectClick(position: Int, volume: Volume, onClick: () -> Unit) {
        if (selectedVolumes.isEmpty()) {
            onClick()
            closeSelectionMode()
        } else {
            if (selectedVolumes.contains(volume)) {
                selectedVolumes.remove(volume)
                viewState.onItemUnselected(volume, position)
            } else {
                selectedVolumes.add(volume)
                viewState.onItemSelected(volume, position)
            }
            viewState.showSelectionMode(selectedVolumes.size)
        }
    }

    private fun closeSelectionMode() {
        selectedVolumes.clear()
        viewState.showSelectionMode(0)
        viewState.setItemsSelected(false)
    }

    private fun addPreparedVolumesToPlayList(playList: Playlist) {
        performAddToPlaylist(
            interactor.getAllCompositionsInFileSources(volumesForPlayList),
            playList
        ) { onAddingToPlayListCompleted() }
    }

    private fun onAddingToPlayListCompleted() {
        volumesForPlayList.clear()
        if (selectedVolumes.isNotEmpty()) {
            closeSelectionMode()
        }
    }

    private fun subscribeOnVolumes() {
        if (volumeList.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(volumesDisposable, presenterDisposable)
        volumesDisposable = interactor.getVolumes()
            .observeOn(uiScheduler)
            .subscribe(this::onVolumesLoaded, this::onVolumesLoadingError)
        presenterDisposable.add(volumesDisposable!!)
    }

    private fun onVolumesLoaded(volumes: List<Volume>) {
        this.volumeList = volumes
        viewState.updateList(volumes)
        if (volumes.isEmpty()) {
            viewState.showEmptyList()
        } else {
            viewState.showList()
        }
    }

    private fun onVolumesLoadingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showError(errorCommand)
    }

}