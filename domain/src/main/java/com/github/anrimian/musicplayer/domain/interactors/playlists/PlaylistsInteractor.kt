package com.github.anrimian.musicplayer.domain.interactors.playlists

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlaylistFileNameValidator
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlaylistNameValidator
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.menu.AppMenu
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.repositories.PlaylistsRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class PlaylistsInteractor(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsRepository: PlaylistsRepository,
    private val settingsRepository: SettingsRepository,
    private val uiStateRepository: UiStateRepository,
    private val analytics: Analytics
) {

    private val nameValidator = PlaylistNameValidator()

    fun getPlaylistsObservable(searchQuery: String? = null): Observable<List<Playlist>> {
        return playListsRepository.getPlaylistsObservable(searchQuery)
    }

    fun getPlaylistFlow(playListId: Long): Flow<Playlist> {
        return playListsRepository.getPlaylistObservable(playListId).asFlow()
    }

    fun getCompositionsObservable(
        playlistId: Long,
        searchText: String?
    ): Observable<List<PlaylistEntry>> {
        return playListsRepository.getCompositionsObservable(playlistId, searchText)
    }

    fun createPlaylist(name: String): Single<Playlist> {
        return nameValidator.validate(PlaylistFileNameValidator.normalizePlayListName(name))
            .flatMap(playListsRepository::createPlaylist)
    }

    fun addCompositionsToPlaylist(
        compositions: List<CompositionModel>,
        playList: Playlist,
        checkForDuplicates: Boolean,
        ignoreDuplicates: Boolean
    ): Single<List<CompositionModel>> {
        val duplicateCheck = checkForDuplicates && settingsRepository.isPlaylistDuplicateCheckEnabled
        return playListsRepository.addCompositionsToPlaylist(
            compositions,
            playList,
            duplicateCheck,
            ignoreDuplicates
        )
    }

    fun deleteItemFromPlaylist(playListEntry: PlaylistEntry, playListId: Long): Completable {
        return playListsRepository.deleteItemFromPlaylist(playListEntry, playListId)
    }

    fun restoreDeletedPlaylistItem(): Completable {
        return playListsRepository.restoreDeletedPlaylistItem()
    }

    suspend fun deletePlaylist(playListId: Long) {
        playListsRepository.deletePlaylist(playListId).await()
    }

    suspend fun deletePlaylists(ids: LongArray) {
        ids.forEach { id -> playListsRepository.deletePlaylist(id).await() }
    }

    fun moveItemInPlaylist(playListId: Long, from: Int, to: Int): Completable {
        return playListsRepository.moveItemInPlaylist(playListId, from, to)
            .doOnError(analytics::processNonFatalError)
    }

    suspend fun sortPlaylistEntries(playlistId: Long, order: Order) {
        playListsRepository.sortPlaylistEntries(
            playlistId,
            order,
            settingsRepository.isDisplayFileNameEnabled
        ).await()
    }

    suspend fun undoSortPlaylistEntries() {
        playListsRepository.undoSortPlaylistEntries().await()
    }

    suspend fun updatePlaylistName(playListId: Long, name: String) {
        val name = nameValidator.validate(PlaylistFileNameValidator.normalizePlayListName(name)).await()
        playListsRepository.updatePlaylistName(playListId, name).await()
    }

    fun setSelectedPlaylistScreen(playListId: Long) {
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

    suspend fun exportPlaylistsToFolder(playlists: List<Playlist>, folder: FileReference) {
        playListsRepository.exportPlaylistsToFolder(playlists, folder).await()
    }

    suspend fun importPlaylistFile(file: FileReference, overwriteExisting: Boolean): Long {
        return playListsRepository.importPlaylistFile(file, overwriteExisting).await()
    }

    suspend fun startPlaying(playlists: List<Playlist>) {
        val ids = playListsRepository.getCompositionIdsInPlaylists(playlists).await()
        playerInteractor.setQueueAndPlay(ids).await()
    }

    suspend fun getCompositionsByPlaylistsIds(playlistIds: LongArray): List<Composition> {
        return playListsRepository.getCompositionsByPlaylistsIds(playlistIds.asIterable()).await()
    }

    suspend fun getCompositionsInPlaylists(playlists: List<Playlist>): List<Composition> {
        return playListsRepository.getCompositionsInPlaylists(playlists).await()
    }

    fun getPlaylistMenuConfigFlow() = settingsRepository.getMenuConfigObservable(AppMenu.PLAYLIST)
        .asFlow()
        .map { opt -> opt.value }

    fun getPlaylistEntryMenuConfigFlow() = settingsRepository.getMenuConfigObservable(AppMenu.PLAYLIST_ENTRY)
        .asFlow()
        .map { opt -> opt.value }

}