package com.github.anrimian.musicplayer.domain.interactors.playlists

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlayListFileNameValidator
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlayListNameValidator
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class PlayListsInteractor(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsRepository: PlayListsRepository,
    private val settingsRepository: SettingsRepository,
    private val uiStateRepository: UiStateRepository,
    private val analytics: Analytics
) {

    private val nameValidator = PlayListNameValidator()

    fun getPlayListsObservable(searchQuery: String? = null): Observable<List<PlayList>> {
        return playListsRepository.getPlayListsObservable(searchQuery)
    }

    fun getPlayListObservable(playListId: Long): Observable<PlayList> {
        return playListsRepository.getPlayListObservable(playListId)
    }

    fun getCompositionsObservable(
        playlistId: Long,
        searchText: String?
    ): Observable<List<PlayListItem>> {
        return playListsRepository.getCompositionsObservable(playlistId, searchText)
    }

    fun createPlayList(name: String): Single<PlayList> {
        return nameValidator.validate(PlayListFileNameValidator.normalizePlayListName(name))
            .flatMap(playListsRepository::createPlayList)
    }

    fun addCompositionsToPlayList(
        compositions: List<Composition>,
        playList: PlayList,
        checkForDuplicates: Boolean,
        ignoreDuplicates: Boolean
    ): Single<List<Composition>> {
        val duplicateCheck = checkForDuplicates && settingsRepository.isPlaylistDuplicateCheckEnabled
        return playListsRepository.addCompositionsToPlayList(
            compositions,
            playList,
            duplicateCheck,
            ignoreDuplicates
        )
    }

    fun deleteItemFromPlayList(playListItem: PlayListItem, playListId: Long): Completable {
        return playListsRepository.deleteItemFromPlayList(playListItem, playListId)
    }

    fun restoreDeletedPlaylistItem(): Completable {
        return playListsRepository.restoreDeletedPlaylistItem()
    }

    fun deletePlayList(playListId: Long): Completable {
        return playListsRepository.deletePlayList(playListId)
    }

    fun deletePlayLists(playlists: Collection<PlayList>): Completable {
        return Observable.fromIterable(playlists)
            .flatMapCompletable { playlist -> playListsRepository.deletePlayList(playlist.id) }
    }

    fun moveItemInPlayList(playListId: Long, from: Int, to: Int): Completable {
        return playListsRepository.moveItemInPlayList(playListId, from, to)
            .doOnError(analytics::processNonFatalError)
    }

    fun updatePlayListName(playListId: Long, name: String): Completable {
        return nameValidator.validate(PlayListFileNameValidator.normalizePlayListName(name))
            .flatMapCompletable { playListsRepository.updatePlayListName(playListId, name) }
    }

    fun setSelectedPlayListScreen(playListId: Long) {
        uiStateRepository.selectedPlayListScreenId = playListId
    }

    fun saveListPosition(listPosition: ListPosition?) {
        uiStateRepository.savePlaylistsPosition(listPosition)
    }

    fun getSavedListPosition(): ListPosition? = uiStateRepository.savedPlaylistsPosition

    fun saveItemsListPosition(playListId: Long, listPosition: ListPosition) {
        uiStateRepository.savePlaylistsListPosition(playListId, listPosition)
    }

    fun getSavedItemsListPosition(playListId: Long): ListPosition? {
        return uiStateRepository.getSavedPlaylistListPosition(playListId)
    }

    fun isPlaylistDuplicateCheckEnabled() = settingsRepository.isPlaylistDuplicateCheckEnabled

    fun setPlaylistDuplicateCheckEnabled(isEnabled: Boolean) {
        settingsRepository.isPlaylistDuplicateCheckEnabled = isEnabled
    }

    fun exportPlaylistsToFolder(playlists: List<PlayList>, folder: FileReference): Completable {
        return playListsRepository.exportPlaylistsToFolder(playlists, folder)
    }

    fun importPlaylistFile(file: FileReference, overwriteExisting: Boolean): Single<Long> {
        return playListsRepository.importPlaylistFile(file, overwriteExisting)
    }

    fun startPlaying(playlists: List<PlayList>): Completable {
        return playListsRepository.getCompositionIdsInPlaylists(playlists)
            .flatMapCompletable(playerInteractor::setQueueAndPlay)
    }

    fun getCompositionsByPlaylistsIds(playlistIds: LongArray): Single<List<Composition>> {
        return playListsRepository.getCompositionsByPlaylistsIds(playlistIds.asIterable())
    }

    fun getCompositionsInPlaylists(playlists: List<PlayList>): Single<List<Composition>> {
        return playListsRepository.getCompositionsInPlaylists(playlists)
    }

}