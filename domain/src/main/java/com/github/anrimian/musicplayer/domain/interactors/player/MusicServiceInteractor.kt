package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryCompositionsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class MusicServiceInteractor {

    private final CommonPlayerInteractor commonPlayerInteractor;
    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final LibraryCompositionsInteractor libraryCompositionsInteractor;
    private final LibraryFoldersInteractor libraryFoldersInteractor;
    private final LibraryArtistsInteractor libraryArtistsInteractor;
    private final LibraryAlbumsInteractor libraryAlbumsInteractor;
    private final LibraryGenresInteractor libraryGenresInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final SettingsRepository settingsRepository;

    public MusicServiceInteractor(CommonPlayerInteractor commonPlayerInteractor,
                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                  LibraryCompositionsInteractor libraryCompositionsInteractor,
                                  LibraryFoldersInteractor libraryFoldersInteractor,
                                  LibraryArtistsInteractor libraryArtistsInteractor,
                                  LibraryAlbumsInteractor libraryAlbumsInteractor,
                                  LibraryGenresInteractor libraryGenresInteractor,
                                  PlayListsInteractor playListsInteractor,
                                  SettingsRepository settingsRepository) {
        this.commonPlayerInteractor = commonPlayerInteractor;
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.libraryCompositionsInteractor = libraryCompositionsInteractor;
        this.libraryFoldersInteractor = libraryFoldersInteractor;
        this.libraryArtistsInteractor = libraryArtistsInteractor;
        this.libraryAlbumsInteractor = libraryAlbumsInteractor;
        this.libraryGenresInteractor = libraryGenresInteractor;
        this.playListsInteractor = playListsInteractor;
        this.settingsRepository = settingsRepository;
    }

    public void prepare() {
        commonPlayerInteractor.prepare();
    }

    public void play(long delay, @Nullable PlayerType forcePlayerType) {
        commonPlayerInteractor.play(delay, forcePlayerType);
    }

    public void skipToNext() {
        commonPlayerInteractor.skipToNext();
    }

    public void skipToPrevious() {
        commonPlayerInteractor.skipToPrevious();
    }

    public void setRepeatMode(int appRepeatMode) {
        commonPlayerInteractor.setRepeatMode(appRepeatMode);
    }

    public void changeRandomMode() {
        commonPlayerInteractor.changeRandomMode();
    }

    public void changeRepeatMode() {
        commonPlayerInteractor.changeRepeatMode();
    }

    public void setRandomPlayingEnabled(boolean isEnabled) {
        commonPlayerInteractor.setRandomPlayingEnabled(isEnabled);
    }

    public void setPlaybackSpeed(float speed) {
        commonPlayerInteractor.setPlaybackSpeed(speed);
    }

    public void fastSeekBackward() {
        commonPlayerInteractor.fastSeekBackward();
    }

    public void fastSeekForward() {
        commonPlayerInteractor.fastSeekForward();
    }

    public void reset() {
        commonPlayerInteractor.reset();
    }

    public Observable<Long> getTrackPositionChangeObservable() {
        return commonPlayerInteractor.getTrackPositionChangeObservable();
    }

    public Single<Long> getTrackPosition() {
        return commonPlayerInteractor.getTrackPosition();
    }

    public Completable shuffleAllAndPlay() {
        return libraryCompositionsInteractor.getCompositionsObservable(null)
                .firstOrError()
                .flatMapCompletable(compositions -> {
                    libraryPlayerInteractor.setRandomPlayingEnabled(true);
                    return libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions);
                });
    }

    public Completable playFromSearch(@Nullable String searchQuery) {
        return playFromSearch(searchQuery, 0);
    }

    public Completable playFromSearch(@Nullable String searchQuery, int position) {
        return libraryCompositionsInteractor.getCompositionsObservable(searchQuery)
                .firstOrError()
                .flatMapCompletable(compositions ->
                        libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
                );
    }

    public Observable<Float> getPlaybackSpeedObservable() {
        return commonPlayerInteractor.getPlaybackSpeedObservable();
    }

    public Observable<Integer> getRepeatModeObservable() {
        return commonPlayerInteractor.getRepeatModeObservable();
    }

    public Observable<Boolean> getRandomModeObservable() {
        return commonPlayerInteractor.getRandomModeObservable();
    }

    public Observable<MusicNotificationSetting> getNotificationSettingObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                getColoredNotificationEnabledObservable(),
                getNotificationCoverStubEnabledObservable(),
                getCoversOnLockScreenEnabledObservable(),
                MusicNotificationSetting::new);
    }

    public Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return libraryCompositionsInteractor.getCompositionsObservable(searchText);
    }

    public Completable startPlayingFromCompositions(int position) {
        return libraryCompositionsInteractor.getCompositionsObservable(null)
                .firstOrError()
                .flatMapCompletable(compositions ->
                        libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
                );
    }

    public Observable<List<FileSource>> getFoldersObservable(@Nullable Long folderId) {
        return libraryFoldersInteractor.getFoldersInFolder(folderId, null);
    }

    public Completable play(@Nullable Long folderId, long compositionId) {
        return libraryFoldersInteractor.play(folderId, compositionId);
    }

    public Observable<List<Artist>> getArtistsObservable() {
        return libraryArtistsInteractor.getArtistsObservable(null);
    }

    public Observable<List<Composition>> getCompositionsByArtist(long artistId) {
        return libraryArtistsInteractor.getCompositionsByArtist(artistId);
    }

    public Completable startPlayingFromArtistCompositions(long artistId, int position) {
        return getCompositionsByArtist(artistId)
                .firstOrError()
                .flatMapCompletable(compositions ->
                        libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
                );
    }

    public Observable<List<Album>> getAlbumsObservable() {
        return libraryAlbumsInteractor.getAlbumsObservable(null);
    }

    public Observable<List<AlbumComposition>> getAlbumItemsObservable(long albumId) {
        return libraryAlbumsInteractor.getAlbumItemsObservable(albumId);
    }

    public Completable startPlayingFromAlbumCompositions(long albumId, int position) {
        return getAlbumItemsObservable(albumId)
                .firstOrError()
                .flatMapCompletable(compositions ->
                        libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
                );
    }

    public Observable<List<Genre>> getGenresObservable() {
        return libraryGenresInteractor.getGenresObservable(null);
    }

    public Observable<List<Composition>> getGenreItemsObservable(long genreId) {
        return libraryGenresInteractor.getGenreItemsObservable(genreId);
    }

    public Completable startPlayingFromGenreCompositions(long genreId, int position) {
        return getGenreItemsObservable(genreId)
                .firstOrError()
                .flatMapCompletable(compositions ->
                        libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
                );
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return playListsInteractor.getPlayListsObservable(null);
    }

    public Observable<List<PlayListItem>> getPlaylistItemsObservable(long playListId) {
        return playListsInteractor.getCompositionsObservable(playListId, null);
    }

    public Completable startPlayingFromPlaylistItems(long playListId, int position) {
        return getPlaylistItemsObservable(playListId)
                .firstOrError()
                .flatMapCompletable(compositions ->
                        libraryPlayerInteractor.setCompositionsQueueAndPlay(compositions, position)
                );
    }

    public MusicNotificationSetting getNotificationSettings() {
        boolean coversInNotification = isCoversInNotificationEnabled();
        boolean coloredNotification = settingsRepository.isColoredNotificationEnabled();
        boolean showNotificationCoverStub = settingsRepository.isNotificationCoverStubEnabled();
        boolean coversOnLockScreen = settingsRepository.isCoversOnLockScreenEnabled();
        return new MusicNotificationSetting(
                coversInNotification,
                coversInNotification && coloredNotification,
                coversInNotification && showNotificationCoverStub,
                coversInNotification && coversOnLockScreen
        );
    }

    public boolean isCoversInNotificationEnabled() {
        return settingsRepository.isCoversEnabled()
                && settingsRepository.isCoversInNotificationEnabled();
    }

    private Observable<Boolean> getCoversInNotificationEnabledObservable() {
        return Observable.combineLatest(settingsRepository.getCoversEnabledObservable(),
                settingsRepository.getCoversInNotificationEnabledObservable(),
                (coversEnabled, coversInNotification) -> coversEnabled && coversInNotification);
    }

    private Observable<Boolean> getColoredNotificationEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getColoredNotificationEnabledObservable(),
                (coversInNotification, coloredNotification) -> coversInNotification && coloredNotification);
    }

    private Observable<Boolean> getNotificationCoverStubEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getNotificationCoverStubEnabledObservable(),
                (coversInNotification, showNotificationCoverStub) -> coversInNotification && showNotificationCoverStub);
    }

    private Observable<Boolean> getCoversOnLockScreenEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getCoversOnLockScreenEnabledObservable(),
                (coversInNotification, coversOnLockScreen) -> coversInNotification && coversOnLockScreen);
    }

}
