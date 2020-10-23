package com.github.anrimian.musicplayer.domain.interactors.library;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class LibraryArtistsInteractor {

    private final LibraryRepository musicProviderRepository;
    private final EditorRepository editorRepository;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;

    public LibraryArtistsInteractor(LibraryRepository musicProviderRepository,
                                    EditorRepository editorRepository,
                                    SettingsRepository settingsRepository,
                                    UiStateRepository uiStateRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.editorRepository = editorRepository;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
    }

    public Observable<List<Artist>> getArtistsObservable(@Nullable String searchText) {
        return musicProviderRepository.getArtistsObservable(searchText);
    }

    public Observable<List<Composition>> getCompositionsByArtist(long artistId) {
        return musicProviderRepository.getCompositionsByArtist(artistId);
    }

    public Observable<Artist> getArtistObservable(long artistId) {
        return musicProviderRepository.getArtistObservable(artistId);
    }

    public Observable<List<Album>> getAllAlbumsForArtist(long artistId) {
        return musicProviderRepository.getAllAlbumsForArtist(artistId);
    }

    public Completable updateArtistName(String name, long artistId) {
        return editorRepository.updateArtistName(name, artistId);
    }

    public Order getOrder() {
        return settingsRepository.getArtistsOrder();
    }

    public void setOrder(Order order) {
        settingsRepository.setArtistsOrder(order);
    }

    public ListPosition getSavedListPosition() {
        return uiStateRepository.getSavedArtistsListPosition();
    }

    public void saveListPosition(ListPosition listPosition) {
        uiStateRepository.saveArtistsListPosition(listPosition);
    }
}
