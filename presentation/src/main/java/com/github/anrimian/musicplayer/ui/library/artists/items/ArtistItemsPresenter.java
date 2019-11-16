package com.github.anrimian.musicplayer.ui.library.artists.items;

import com.github.anrimian.musicplayer.domain.business.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.business.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import moxy.InjectViewState;

@InjectViewState
public class ArtistItemsPresenter extends BaseLibraryCompositionsPresenter<ArtistItemsView> {

    private final long artistId;
    private final LibraryArtistsInteractor interactor;

    public ArtistItemsPresenter(long artistId,
                                LibraryArtistsInteractor interactor,
                                PlayListsInteractor playListsInteractor,
                                MusicPlayerInteractor playerInteractor,
                                DisplaySettingsInteractor displaySettingsInteractor,
                                ErrorParser errorParser,
                                Scheduler uiScheduler) {
        super(playerInteractor,
                playListsInteractor,
                displaySettingsInteractor,
                errorParser,
                uiScheduler);
        this.artistId = artistId;
        this.interactor = interactor;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnArtistInfo();
    }

    @Override
    protected Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return interactor.getCompositionsByArtist(artistId);
    }

    void onFragmentMovedToTop() {
        //save selected screen. Wait a little for all screens
    }

    private void subscribeOnArtistInfo() {
        presenterDisposable.add(interactor.getArtistObservable(artistId)
                .observeOn(uiScheduler)
                .subscribe(this::onArtistInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onArtistInfoReceived(Artist artist) {
        getViewState().showArtistInfo(artist);
    }
}
