package com.github.anrimian.musicplayer.ui.library.folders

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created on 23.10.2017.
 */

class LibraryFoldersPresenter(private val folderId: Long?,
                              private val interactor: LibraryFoldersScreenInteractor,
                              private val playerInteractor: MusicPlayerInteractor,
                              private val displaySettingsInteractor: DisplaySettingsInteractor,
                              errorParser: ErrorParser,
                              uiScheduler: Scheduler)
    : AppPresenter<LibraryFoldersView>(uiScheduler, errorParser) {

    private val presenterBatterySafeDisposable = CompositeDisposable()

    private var currentCompositionDisposable: Disposable? = null

    private var sourceList: List<FileSource> = ArrayList()
    private val filesForPlayList: MutableList<FileSource> = LinkedList()
    private val filesToDelete: MutableList<FileSource> = LinkedList()
    private val selectedFiles = LinkedHashSet<FileSource>()

    private var searchText: String? = null
    private var currentComposition: Composition? = null
    private var recentlyAddedIgnoredFolder: IgnoredFolder? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showSearchMode(false)
        viewState.hideProgressDialog()
        subscribeOnFolder()
        subscribeOnChildFolders()
        subscribeOnUiSettings()
        subscribeOnMoveEnabledState()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenterDisposable.dispose()
    }

    fun onStart() {
        if (sourceList.isNotEmpty()) {
            subscribeOnCurrentComposition()
        }
    }

    fun onStop() {
        presenterBatterySafeDisposable.clear()
    }

    fun onTryAgainButtonClicked() {
        subscribeOnChildFolders()
    }

    fun onCompositionClicked(position: Int, musicFileSource: CompositionFileSource) {
        processMultiSelectClick(position, musicFileSource) {
            val composition = musicFileSource.composition
            viewState.showCompositionActionDialog(composition)
        }
    }

    fun onCompositionIconClicked(composition: Composition) {
        if (composition == currentComposition) {
            playerInteractor.playOrPause()
        } else {
            interactor.play(folderId, composition)
            viewState.showCurrentComposition(CurrentComposition(composition, true))
        }
    }

    fun onPlayActionSelected(composition: Composition?) {
        interactor.play(folderId, composition)
    }

    fun onPlayNextCompositionClicked(composition: Composition) {
        addCompositionsToPlayNext(ListUtils.asList(composition))
    }

    fun onAddToQueueCompositionClicked(composition: Composition) {
        addCompositionsToEnd(ListUtils.asList(composition))
    }

    fun onPlayFolderClicked(folder: FolderFileSource) {
        interactor.play(listOf(folder))
    }

    fun onPlayNextFolderClicked(folder: FolderFileSource) {
        interactor.getAllCompositionsInFolder(folder.id)
                .flatMap(playerInteractor::addCompositionsToPlayNext)
                .subscribeOnUi(viewState::onCompositionsAddedToPlayNext, this::onDefaultError)
    }

    fun onAddToQueueFolderClicked(folder: FolderFileSource) {
        interactor.getAllCompositionsInFolder(folder.id)
                .flatMap(playerInteractor::addCompositionsToEnd)
                .subscribeOnUi(viewState:: onCompositionsAddedToQueue, this::onDefaultError)
    }

    fun onPlayAllButtonClicked() {
        if (selectedFiles.isEmpty()) {
            interactor.playAllMusicInFolder(folderId)
        } else {
            playSelectedCompositions()
        }
    }

    fun onBackPathButtonClicked() {
        checkNotNull(folderId) { "can not go back in root screen" }
        closeSelectionMode()
        goBackToPreviousScreen()
    }

    fun onDeleteCompositionButtonClicked(composition: Composition) {
        filesToDelete.clear()
        filesToDelete.add(CompositionFileSource(composition))
        viewState.showConfirmDeleteDialog(ListUtils.asList(composition))
    }

    fun onDeleteFolderButtonClicked(folder: FolderFileSource?) {
        viewState.showConfirmDeleteDialog(folder)
    }

    fun onDeleteCompositionsDialogConfirmed() {
        deletePreparedFiles()
    }

    fun onDeleteFolderDialogConfirmed(folder: FolderFileSource?) {
        interactor.deleteFolder(folder)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showDeleteProgress() }
                .doFinally { viewState.hideProgressDialog() }
                .subscribeOnUi(this::onDeleteFolderSuccess, this::onDeleteCompositionsError)
    }

    fun onOrderMenuItemClicked() {
        viewState.showSelectOrderScreen(interactor.folderOrder)
    }

    fun onOrderSelected(order: Order?) {
        interactor.folderOrder = order
    }

    fun onAddToPlayListButtonClicked(composition: Composition?) {
        filesForPlayList.clear()
        filesForPlayList.add(CompositionFileSource(composition))
        viewState.showSelectPlayListDialog()
    }

    fun onPlayListToAddingSelected(playList: PlayList) {
        addPreparedCompositionsToPlayList(playList)
    }

    fun onAddFolderToPlayListButtonClicked(folder: FolderFileSource?) {
        viewState.showSelectPlayListForFolderDialog(folder)
    }

    fun onPlayListForFolderSelected(folderId: Long?, playList: PlayList?) {
        interactor.addCompositionsToPlayList(folderId, playList)
                .subscribeOnUi(
                        { addedCompositions -> viewState.showAddingToPlayListComplete(playList, addedCompositions) },
                        this::onAddingToPlayListError
                )
    }

    fun onSearchTextChanged(text: String?) {
        if (searchText != text) {
            searchText = text
            subscribeOnChildFolders()
        }
    }

    fun onSearchButtonClicked() {
        viewState.showSearchMode(true)
    }

    fun onShareFolderClicked(folder: FolderFileSource) {
        shareFileSources(ListUtils.asList<FileSource>(folder))
    }

    fun onFolderClicked(position: Int, folder: FolderFileSource) {
        processMultiSelectClick(position, folder) { viewState.goToMusicStorageScreen(folder.id) }
    }

    fun onItemLongClick(position: Int, folder: FileSource) {
        interactor.stopMoveMode()
        viewState.updateMoveFilesList()
        selectedFiles.add(folder)
        viewState.showSelectionMode(selectedFiles.size)
        viewState.onItemSelected(folder, position)
    }

    fun onFragmentDisplayed() {
        interactor.saveCurrentFolder(folderId)
    }

    fun onRenameFolderClicked(folder: FolderFileSource?) {
        viewState.showInputFolderNameDialog(folder)
    }

    fun onNewFolderNameEntered(folderId: Long, name: String?) {
        interactor.renameFolder(folderId, name)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showRenameProgress() }
                .doFinally { viewState.hideProgressDialog() }
                .justSubscribe(this::onDefaultError)
    }

    fun onSelectionModeBackPressed() {
        closeSelectionMode()
    }

    fun onPlayAllSelectedClicked() {
        playSelectedCompositions()
    }

    fun onSelectAllButtonClicked() {
        selectedFiles.clear() //reselect previous feature
        selectedFiles.addAll(sourceList)
        viewState.showSelectionMode(selectedFiles.size)
        viewState.setItemsSelected(true)
    }

    private fun playSelectedCompositions() {
        interactor.play(ArrayList(selectedFiles))
        closeSelectionMode()
    }

    fun onPlayNextSelectedSourcesClicked() {
        interactor.addCompositionsToPlayNext(ArrayList(selectedFiles))
                .subscribeOnUi(viewState::onCompositionsAddedToPlayNext, this::onDefaultError)
        closeSelectionMode()
    }

    fun onAddToQueueSelectedSourcesClicked() {
        interactor.addCompositionsToEnd(ArrayList(selectedFiles))
                .subscribeOnUi(viewState::onCompositionsAddedToQueue, this::onDefaultError)
        closeSelectionMode()
    }

    fun onAddSelectedSourcesToPlayListClicked() {
        filesForPlayList.clear()
        filesForPlayList.addAll(selectedFiles)
        viewState.showSelectPlayListDialog()
    }

    fun onShareSelectedSourcesClicked() {
        shareFileSources(ArrayList(selectedFiles))
    }

    fun onDeleteSelectedCompositionButtonClicked() {
        filesToDelete.clear()
        filesToDelete.addAll(selectedFiles)
        interactor.getAllCompositionsInFileSources(ArrayList(selectedFiles))
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showDeleteProgress() }
                .doFinally { viewState.hideProgressDialog() }
                .subscribeOnUi(viewState::showConfirmDeleteDialog, this::onDefaultError)
    }

    fun onMoveSelectedFoldersButtonClicked() {
        interactor.addFilesToMove(folderId, selectedFiles)
        closeSelectionMode()
        viewState.updateMoveFilesList()
    }

    fun onCopySelectedFoldersButtonClicked() {
        interactor.addFilesToCopy(folderId, selectedFiles)
        closeSelectionMode()
    }

    fun onCloseMoveMenuClicked() {
        interactor.stopMoveMode()
        viewState.updateMoveFilesList()
    }

    fun onPasteButtonClicked() {
        interactor.moveFilesTo(folderId)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showMoveProgress() }
                .doFinally { viewState.hideProgressDialog() }
                .subscribeOnUi(viewState::updateMoveFilesList, this::onDefaultError)
    }

    fun onPasteInNewFolderButtonClicked() {
        viewState.showInputNewFolderNameDialog()
    }

    fun onNewFileNameForPasteEntered(name: String?) {
        interactor.moveFilesToNewFolder(folderId, name)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showMoveProgress() }
                .doFinally { viewState.hideProgressDialog() }
                .subscribeOnUi(viewState::updateMoveFilesList, this::onDefaultError)
    }

    fun onExcludeFolderClicked(folder: FolderFileSource?) {
        interactor.addFolderToIgnore(folder)
                .subscribeOnUi(this::onIgnoreFolderAdded, this::onDefaultError)
    }

    fun onRemoveIgnoredFolderClicked() {
        interactor.deleteIgnoredFolder(recentlyAddedIgnoredFolder)
                .justSubscribe(this::onDefaultError)
    }

    fun getSelectedMoveFiles(): LinkedHashSet<FileSource> = interactor.filesToMove

    fun getSelectedFiles() = selectedFiles

    private fun onIgnoreFolderAdded(folder: IgnoredFolder) {
        recentlyAddedIgnoredFolder = folder
        viewState.showAddedIgnoredFolderMessage(folder)
    }

    private fun shareFileSources(fileSources: List<FileSource>) {
        interactor.getAllCompositionsInFileSources(fileSources)
                .subscribeOnUi(viewState::sendCompositions, this::onReceiveCompositionsError)
    }

    private fun processMultiSelectClick(position: Int, folder: FileSource, onClick: () -> Unit) {
        if (selectedFiles.isEmpty()) {
            onClick()
            closeSelectionMode()
        } else {
            if (selectedFiles.contains(folder)) {
                selectedFiles.remove(folder)
                viewState.onItemUnselected(folder, position)
            } else {
                selectedFiles.add(folder)
                viewState.onItemSelected(folder, position)
            }
            viewState.showSelectionMode(selectedFiles.size)
        }
    }

    private fun closeSelectionMode() {
        selectedFiles.clear()
        viewState.showSelectionMode(0)
        viewState.setItemsSelected(false)
    }

    private fun addCompositionsToPlayNext(compositions: List<Composition>) {
        playerInteractor.addCompositionsToPlayNext(compositions)
                .subscribeOnUi(viewState::onCompositionsAddedToPlayNext, this::onDefaultError)
    }

    private fun addCompositionsToEnd(compositions: List<Composition>) {
        playerInteractor.addCompositionsToEnd(compositions)
                .subscribeOnUi(viewState::onCompositionsAddedToQueue, this::onDefaultError)
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun onReceiveCompositionsError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showReceiveCompositionsForSendError(errorCommand)
    }

    private fun deletePreparedFiles() {
        interactor.deleteFiles(filesToDelete)
                .subscribeOnUi(this::onDeleteCompositionsSuccess, this::onDeleteCompositionsError)
    }

    private fun onDeleteFolderSuccess(deletedCompositions: List<Composition>) {
        viewState.showDeleteCompositionMessage(deletedCompositions)
    }

    private fun onDeleteCompositionsSuccess(compositions: List<Composition>) {
        viewState.showDeleteCompositionMessage(compositions)
        filesToDelete.clear()
        if (!selectedFiles.isEmpty()) {
            closeSelectionMode()
        }
    }

    private fun onDeleteCompositionsError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun addPreparedCompositionsToPlayList(playList: PlayList) {
        interactor.addCompositionsToPlayList(filesForPlayList, playList)
                .subscribeOnUi(
                        { compositions -> onAddingToPlayListCompleted(compositions, playList) },
                        this::onAddingToPlayListError
                )
    }

    private fun onAddingToPlayListError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showAddingToPlayListError(errorCommand)
    }

    private fun onAddingToPlayListCompleted(compositions: List<Composition>, playList: PlayList) {
        viewState.showAddingToPlayListComplete(playList, compositions)
        filesForPlayList.clear()
        if (!selectedFiles.isEmpty()) {
            closeSelectionMode()
        }
    }

    private fun goBackToPreviousScreen() {
        if (folderId != null) {
            viewState.goBackToParentFolderScreen()
        }
    }

    private fun subscribeOnFolder() {
        if (folderId == null) {
            viewState.hideFolderInfo()
            return
        }
        presenterDisposable.add(interactor.getFolderObservable(folderId)
                .observeOn(uiScheduler)
                .subscribe({ folder: FolderFileSource? -> viewState.showFolderInfo(folder) }, { throwable: Throwable -> onDefaultError(throwable) }) { goBackToPreviousScreen() }
        )
    }

    private fun subscribeOnChildFolders() {
        if (sourceList.isEmpty()) {
            viewState.showLoading()
        }
        interactor.getFoldersInFolder(folderId, searchText)
                .subscribeOnUi(this::onFilesLoaded, this::onMusicLoadingError)
    }

    private fun onFilesLoaded(files: List<FileSource>) {
        sourceList = files
        viewState.updateList(sourceList)
        if (sourceList.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                viewState.showEmptyList()
            } else {
                viewState.showEmptySearchResult()
            }
        } else {
            viewState.showList()
            if (RxUtils.isInactive(currentCompositionDisposable)) {
                subscribeOnCurrentComposition()
            }
        }
    }

    private fun onMusicLoadingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showError(errorCommand)
    }

    private fun subscribeOnCurrentComposition() {
        currentCompositionDisposable = playerInteractor.currentCompositionObservable
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionReceived, errorParser::logError)
        presenterBatterySafeDisposable.add(currentCompositionDisposable!!)
    }

    private fun onCurrentCompositionReceived(currentComposition: CurrentComposition) {
        this.currentComposition = currentComposition.composition
        viewState.showCurrentComposition(currentComposition)
    }

    private fun subscribeOnUiSettings() {
        displaySettingsInteractor.coversEnabledObservable
                .subscribeOnUi(this::onUiSettingsReceived, errorParser::logError)
    }

    private fun onUiSettingsReceived(isCoversEnabled: Boolean) {
        viewState.setDisplayCoversEnabled(isCoversEnabled)
    }

    private fun subscribeOnMoveEnabledState() {
        interactor.moveModeObservable.unsafeSubscribeOnUi(viewState::showMoveFileMenu)
    }
}