package com.github.anrimian.musicplayer.ui.library.compositions;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.library.LibraryCompositionsInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.DiffCalculator;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;

@InjectViewState
public class LibraryCompositionsPresenter extends MvpPresenter<LibraryCompositionsView> {

    private final LibraryCompositionsInteractor interactor;
    private final PlayListsInteractor playListsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable compositionsDisposable;

    private List<Composition> compositions = new ArrayList<>();
    private LinkedHashSet<Composition> selectedCompositions = new LinkedHashSet<>();

    private final DiffCalculator<Composition> diffCalculator = new DiffCalculator<>(
            () -> compositions,
            CompositionHelper::areSourcesTheSame);//TODO update calculator with onDelete feature

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    @Nullable
    private String searchText;

    public LibraryCompositionsPresenter(LibraryCompositionsInteractor interactor,
                                        PlayListsInteractor playListsInteractor,
                                        ErrorParser errorParser,
                                        Scheduler uiScheduler) {
        this.interactor = interactor;
        this.playListsInteractor = playListsInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnCompositions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onCompositionClicked(int position, Composition composition) {
        if (selectedCompositions.isEmpty()) {
            interactor.play(compositions, position);
        } else {
            if (selectedCompositions.contains(composition)) {
                selectedCompositions.remove(composition);
                getViewState().onCompositionUnselected(composition, position);
            } else {
                selectedCompositions.add(composition);
                getViewState().onCompositionSelected(composition, position);
            }
        }
    }

    void onPlayAllButtonClicked() {
        if (selectedCompositions.isEmpty()) {
            interactor.play(compositions);
        } else {
            interactor.play(new ArrayList<>(selectedCompositions));
        }
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setOrder(order);
        subscribeOnCompositions();
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListToAddingSelected(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    void onSearchTextChanged(String text) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text;
            subscribeOnCompositions();
        }
    }

    void onCompositionLongClick(int position, Composition composition) {
        selectedCompositions.add(composition);
        getViewState().onCompositionSelected(composition, position);
    }

    boolean onBackPressed() {
        if (selectedCompositions.isEmpty()) {
            return false;
        }
        selectedCompositions.clear();
        getViewState().clearSelectedItems();
        return true;
    }

    private void deletePreparedCompositions() {
        interactor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();
    }

    private void onDeleteCompositionError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();
    }

    private void subscribeOnCompositions() {
        if (compositionsDisposable == null) {
            getViewState().showLoading();
        }
        dispose(compositionsDisposable, presenterDisposable);
        compositionsDisposable = interactor.getCompositionsObservable(searchText)
                .map(diffCalculator::calculateChange)
                .observeOn(uiScheduler)
                .subscribe(this::onCompositionsReceived);
        presenterDisposable.add(compositionsDisposable);
    }

    private void onCompositionsReceived(ListUpdate<Composition> listUpdate) {
        compositions = listUpdate.getNewList();
        getViewState().updateList(listUpdate, selectedCompositions);
        if (compositions.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                getViewState().showEmptyList();
            } else {
                getViewState().showEmptySearchResult();
            }
        } else {
            getViewState().showList();
        }
    }
}
