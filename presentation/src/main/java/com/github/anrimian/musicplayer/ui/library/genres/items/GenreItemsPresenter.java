package com.github.anrimian.musicplayer.ui.library.genres.items;

import com.github.anrimian.musicplayer.domain.business.library.LibraryGenresInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
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
public class GenreItemsPresenter extends MvpPresenter<GenreItemsView> {

    private final long genreId;
    private final LibraryGenresInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable listDisposable;

    private List<Composition> compositions = new ArrayList<>();

    public GenreItemsPresenter(long genreId,
                               LibraryGenresInteractor interactor,
                               ErrorParser errorParser,
                               Scheduler uiScheduler) {
        this.genreId = genreId;
        this.interactor = interactor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnGenresList();
        subscribeOnGenreInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onTryAgainLoadCompositionsClicked() {
        subscribeOnGenresList();
    }

    void onFragmentMovedToTop() {
        //save selected genre screen
    }

    private void subscribeOnGenreInfo() {
        presenterDisposable.add(interactor.getGenreObservable(genreId)
                .observeOn(uiScheduler)
                .subscribe(this::onGenreInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onGenreInfoReceived(Genre genre) {
        getViewState().showGenreInfo(genre);
    }

    private void subscribeOnGenresList() {
        if (compositions.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(listDisposable, presenterDisposable);
        listDisposable = interactor.getGenreItemsObservable(genreId)
                .observeOn(uiScheduler)
                .subscribe(this::onCompositionsReceived, this::onListReceivingError);
        presenterDisposable.add(listDisposable);
    }

    private void onListReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showLoadingError(errorCommand);
    }

    private void onCompositionsReceived(List<Composition> compositions) {
        this.compositions = compositions;
        getViewState().submitList(compositions);
        if (compositions.isEmpty()) {
//            if (TextUtils.isEmpty(searchText)) {
                getViewState().showEmptyList();
//            } else {
//                getViewState().showEmptySearchResult();
//            }
        } else {
            getViewState().showList();
        }
    }
}
