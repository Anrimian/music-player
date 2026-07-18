package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryCompositionsInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class MusicServiceInteractor(
    private val commonPlayerInteractor: CommonPlayerInteractor,
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val libraryCompositionsInteractor: LibraryCompositionsInteractor,
    private val libraryFoldersInteractor: LibraryFoldersInteractor,
    private val libraryArtistsInteractor: LibraryArtistsInteractor,
    private val libraryAlbumsInteractor: LibraryAlbumsInteractor,
    private val libraryGenresInteractor: LibraryGenresInteractor,
    private val playListsInteractor: PlaylistsInteractor,
    private val settingsRepository: SettingsRepository
) {

    fun prepare() {
        commonPlayerInteractor.prepare()
    }

    fun play(delay: Long, forcePlayerType: PlayerType?) {
        commonPlayerInteractor.play(delay, forcePlayerType)
    }

    fun skipToNext() {
        commonPlayerInteractor.skipToNext()
    }

    fun skipToPrevious() {
        commonPlayerInteractor.skipToPrevious()
    }

    fun setRepeatMode(appRepeatMode: Int) {
        commonPlayerInteractor.setRepeatMode(appRepeatMode)
    }

    fun changeRandomMode() {
        commonPlayerInteractor.changeRandomMode()
    }

    fun changeRepeatMode() {
        commonPlayerInteractor.changeRepeatMode()
    }

    fun setRandomPlayingEnabled(isEnabled: Boolean) {
        commonPlayerInteractor.setRandomPlayingEnabled(isEnabled)
    }

    fun setPlaybackSpeed(speed: Float) {
        commonPlayerInteractor.setPlaybackSpeed(speed)
    }

    fun fastSeekBackward() {
        commonPlayerInteractor.fastSeekBackward()
    }

    fun fastSeekForward() {
        commonPlayerInteractor.fastSeekForward()
    }

    fun reset() {
        commonPlayerInteractor.reset()
    }

    fun getTrackPositionChangeObservable(): Observable<Long> {
        return commonPlayerInteractor.getTrackPositionChangeObservable()
    }

    fun getTrackPosition(): Single<Long> = commonPlayerInteractor.getTrackPosition()

    fun shuffleAllAndPlay(): Completable {
        return libraryCompositionsInteractor.getCompositionsObservable(null)
            .firstOrError()
            .flatMapCompletable { compositions ->
                libraryPlayerInteractor.setRandomPlayingEnabled(true)
                libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions)
            }
    }

    fun playFromSearch(searchQuery: String?, position: Int = 0): Completable {
        return libraryCompositionsInteractor.getCompositionsObservable(searchQuery)
            .firstOrError()
            .flatMapCompletable { compositions ->
                libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
            }
    }

    fun getPlaybackSpeedObservable() = commonPlayerInteractor.getPlaybackSpeedObservable()

    fun getRepeatModeObservable() = commonPlayerInteractor.getRepeatModeObservable()

    fun getRandomModeObservable() = commonPlayerInteractor.getRandomModeObservable()

    fun getNotificationSettingObservable(): Observable<MusicNotificationSetting> {
        return Observable.combineLatest(
            getCoversInNotificationEnabledObservable(),
            getColoredNotificationEnabledObservable(),
            getNotificationCoverStubEnabledObservable(),
            getCoversOnLockScreenEnabledObservable(),
            ::MusicNotificationSetting
        )
    }

    fun getCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return libraryCompositionsInteractor.getCompositionsObservable(searchText)
    }

    fun startPlayingFromCompositions(position: Int): Completable {
        return libraryCompositionsInteractor.getCompositionsObservable(null)
            .firstOrError()
            .flatMapCompletable { compositions ->
                libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
            }
    }

    fun getVolumesObservable() = libraryFoldersInteractor.getVolumesObservable()

    fun getFoldersObservable(folderId: Long?): Observable<List<FileSource>> {
        return libraryFoldersInteractor.getFoldersInFolder(folderId, null)
    }

    fun play(folderId: Long?, compositionId: Long): Completable {
        return libraryFoldersInteractor.play(folderId, compositionId)
    }

    fun getArtistsObservable() = libraryArtistsInteractor.getArtistsObservable(null)

    fun getCompositionsByArtist(artistId: Long): Observable<List<Composition>> {
        return libraryArtistsInteractor.getCompositionsByArtist(artistId)
    }

    fun startPlayingFromArtistCompositions(artistId: Long, position: Int): Completable {
        return getCompositionsByArtist(artistId)
            .firstOrError()
            .flatMapCompletable { compositions ->
                libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
            }
    }

    fun getAlbumsObservable() = libraryAlbumsInteractor.getAlbumsObservable(null)

    fun getAlbumItemsObservable(albumId: Long): Observable<List<AlbumComposition>> {
        return libraryAlbumsInteractor.getAlbumItemsObservable(albumId)
    }

    fun startPlayingFromAlbumCompositions(albumId: Long, position: Int): Completable {
        return getAlbumItemsObservable(albumId)
            .firstOrError()
            .flatMapCompletable { compositions ->
                libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
            }
    }

    fun getGenresObservable() = libraryGenresInteractor.getGenresObservable(null)

    fun getGenreItemsObservable(genreId: Long): Observable<List<Composition>> {
        return libraryGenresInteractor.getGenreItemsObservable(genreId)
    }

    fun startPlayingFromGenreCompositions(genreId: Long, position: Int): Completable {
        return getGenreItemsObservable(genreId)
            .firstOrError()
            .flatMapCompletable { compositions ->
                libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
            }
    }

    fun getPlaylistsObservable() = playListsInteractor.getPlaylistsObservable(null)

    fun getPlaylistItemsObservable(playListId: Long): Observable<List<PlaylistEntry>> {
        return playListsInteractor.getCompositionsObservable(playListId, null)
    }

    fun startPlayingFromPlaylistItems(playListId: Long, position: Int): Completable {
        return getPlaylistItemsObservable(playListId)
            .firstOrError()
            .flatMapCompletable { compositions ->
                libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
            }
    }

    fun getNotificationSettings(): MusicNotificationSetting {
        val coversInNotification = isCoversInNotificationEnabled()
        val coloredNotification = settingsRepository.isColoredNotificationEnabled()
        val showNotificationCoverStub = settingsRepository.isNotificationCoverStubEnabled()
        val coversOnLockScreen = settingsRepository.isCoversOnLockScreenEnabled()
        return MusicNotificationSetting(
            coversInNotification,
            coversInNotification && coloredNotification,
            coversInNotification && showNotificationCoverStub,
            coversInNotification && coversOnLockScreen
        )
    }

    fun isCoversInNotificationEnabled(): Boolean {
        return settingsRepository.isCoversEnabled()
                && settingsRepository.isCoversInNotificationEnabled()
    }

    private fun getCoversInNotificationEnabledObservable(): Observable<Boolean> {
        return Observable.combineLatest(
            settingsRepository.getCoversEnabledObservable(),
            settingsRepository.getCoversInNotificationEnabledObservable()
        ) { coversEnabled, coversInNotification -> coversEnabled && coversInNotification }
    }

    private fun getColoredNotificationEnabledObservable(): Observable<Boolean> {
        return Observable.combineLatest(
            getCoversInNotificationEnabledObservable(),
            settingsRepository.getColoredNotificationEnabledObservable()
        ) { coversInNotification, coloredNotification -> coversInNotification && coloredNotification }
    }

    private fun getNotificationCoverStubEnabledObservable(): Observable<Boolean> {
        return Observable.combineLatest(
            getCoversInNotificationEnabledObservable(),
            settingsRepository.getNotificationCoverStubEnabledObservable()
        ) { coversInNotification, showNotificationCoverStub -> coversInNotification && showNotificationCoverStub }
    }

    private fun getCoversOnLockScreenEnabledObservable(): Observable<Boolean> {
        return Observable.combineLatest(
            getCoversInNotificationEnabledObservable(),
            settingsRepository.getCoversOnLockScreenEnabledObservable()
        ) { coversInNotification, coversOnLockScreen -> coversInNotification && coversOnLockScreen }
    }

}
