package com.github.anrimian.musicplayer.ui.library.albums.list;

import com.github.anrimian.musicplayer.domain.business.library.LibraryAlbumsInteractor;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;

@InjectViewState
public class AlbumsListPresenter extends MvpPresenter<AlbumsListView> {

    private final LibraryAlbumsInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable albumsDisposable;
    private Disposable changeDisposable;

    private List<Album> albums = new ArrayList<>();

    public AlbumsListPresenter(LibraryAlbumsInteractor interactor,
                               ErrorParser errorParser,
                               Scheduler uiScheduler) {
        this.interactor = interactor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnAlbumsList();
        getViewState().hideRenameProgress();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onTryAgainLoadCompositionsClicked() {
        subscribeOnAlbumsList();
    }

    void onNewAlbumNameEntered(String name, long albumId) {
        dispose(changeDisposable);
        changeDisposable = interactor.updateAlbumName(name, albumId)
                .observeOn(uiScheduler)
                .doOnSubscribe(d -> getViewState().showRenameProgress())
                .doFinally(() -> getViewState().hideRenameProgress())
                .subscribe(() -> {}, this::onDefaultError);
    }

    private void subscribeOnAlbumsList() {
        if (albums.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(albumsDisposable, presenterDisposable);
        albumsDisposable = interactor.getAlbumsObservable(null)
                .observeOn(uiScheduler)
                .subscribe(this::onAlbumsReceived, this::onAlbumsReceivingError);
        presenterDisposable.add(albumsDisposable);
    }

    private void onAlbumsReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showLoadingError(errorCommand);
    }

    private void onAlbumsReceived(List<Album> albums) {
        this.albums = albums;
        getViewState().submitList(albums);
        if (albums.isEmpty()) {
//            if (TextUtils.isEmpty(searchText)) {
                getViewState().showEmptyList();
//            } else {
//                getViewState().showEmptySearchResult();
//            }
        } else {
            getViewState().showList();
        }
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }
}
