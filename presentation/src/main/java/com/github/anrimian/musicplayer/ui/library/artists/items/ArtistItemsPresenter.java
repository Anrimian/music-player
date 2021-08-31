package com.github.anrimian.musicplayer.ui.library.artists.items;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;


public class ArtistItemsPresenter extends BaseLibraryCompositionsPresenter<ArtistItemsView> {

    private final long artistId;
    private final LibraryArtistsInteractor interactor;

    private Disposable changeDisposable;

    private Artist artist;

    @Nullable
    private Completable lastEditAction;

    public ArtistItemsPresenter(long artistId,
                                LibraryArtistsInteractor interactor,
                                PlayListsInteractor playListsInteractor,
                                LibraryPlayerInteractor playerInteractor,
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
        subscribeOnArtistAlbums();
    }

    @NonNull
    @Override
    protected Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return interactor.getCompositionsByArtist(artistId);
    }

    @Override
    protected ListPosition getSavedListPosition() {
        return interactor.getSavedItemsListPosition(artistId);
    }

    @Override
    protected void saveListPosition(@NonNull ListPosition listPosition) {
        interactor.saveItemsListPosition(artistId, listPosition);
    }

    void onFragmentMovedToTop() {
        //save selected screen. Wait a little for all screens
        if (artist != null) {
            getViewState().showArtistInfo(artist);
        }
    }

    void onRenameArtistClicked() {
        if (artist != null) {
            getViewState().showRenameArtistDialog(artist);
        }
    }

    void onNewArtistNameEntered(String name, long artistId) {
        dispose(changeDisposable);
        lastEditAction = interactor.updateArtistName(name, artistId)
                .observeOn(getUiScheduler())
                .doOnSubscribe(d -> getViewState().showRenameProgress())
                .doFinally(() -> getViewState().hideRenameProgress());
        changeDisposable = lastEditAction.subscribe(() -> {}, this::onDefaultError);
    }

    void onRetryFailedEditActionClicked() {
        if (lastEditAction != null) {
            RxUtils.dispose(changeDisposable, getPresenterDisposable());
            changeDisposable = lastEditAction
                    .doFinally(() -> lastEditAction = null)
                    .subscribe(() -> {}, this::onDefaultError);
            getPresenterDisposable().add(changeDisposable);
        }
    }

    private void subscribeOnArtistInfo() {
        getPresenterDisposable().add(interactor.getArtistObservable(artistId)
                .observeOn(getUiScheduler())
                .subscribe(this::onArtistInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onArtistInfoReceived(Artist artist) {
        this.artist = artist;
        getViewState().showArtistInfo(artist);
    }

    private void subscribeOnArtistAlbums() {
        getPresenterDisposable().add(interactor.getAllAlbumsForArtist(artistId)
                .observeOn(getUiScheduler())
                .subscribe(this::onArtistInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onArtistInfoReceived(List<Album> albums) {
        getViewState().showArtistAlbums(albums);
    }
}
