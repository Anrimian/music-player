package com.github.anrimian.simplemusicplayer.ui.library.compositions;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.library.LibraryCompositionsInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Order;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.simplemusicplayer.data.utils.rx.RxUtils.dispose;
import static com.github.anrimian.simplemusicplayer.domain.utils.ListUtils.asList;

@InjectViewState
public class LibraryCompositionsPresenter extends MvpPresenter<LibraryCompositionsView> {

    private final LibraryCompositionsInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable compositionsDisposable;

    private List<Composition> compositions = new ArrayList<>();

    public LibraryCompositionsPresenter(LibraryCompositionsInteractor interactor,
                                        ErrorParser errorParser,
                                        Scheduler uiScheduler) {
        this.interactor = interactor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindList(compositions);
        subscribeOnCompositions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onCompositionClicked(Composition composition) {
        interactor.play(asList(composition))
                .subscribe();//TODO handle error later
    }

    void onPlayAllButtonClicked() {
        interactor.play(compositions)
                .subscribe();//TODO handle error later
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        interactor.deleteComposition(composition)
                .observeOn(uiScheduler)
                .subscribe();//TODO displayError
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setOrder(order);
        subscribeOnCompositions();
    }

    private void subscribeOnCompositions() {
        if (compositionsDisposable == null) {
            getViewState().showLoading();
        }
        dispose(compositionsDisposable, presenterDisposable);
        compositionsDisposable = interactor.getCompositionsObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCompositionsReceived);
        presenterDisposable.add(compositionsDisposable);
    }

    private void onCompositionsReceived(List<Composition> newCompositions) {
        List<Composition> oldList = new ArrayList<>(compositions);

        compositions.clear();
        compositions.addAll(newCompositions);

        getViewState().updateList(oldList, newCompositions);

        if (newCompositions.isEmpty()) {
            getViewState().showEmptyList();
        } else {
            getViewState().showList();
        }
    }
}
