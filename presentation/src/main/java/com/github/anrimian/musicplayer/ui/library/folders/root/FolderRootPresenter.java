package com.github.anrimian.musicplayer.ui.library.folders.root;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public class FolderRootPresenter extends MvpPresenter<FolderRootView> {

    private final LibraryFilesInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable filesDisposable;

    public FolderRootPresenter(LibraryFilesInteractor interactor,
                               ErrorParser errorParser,
                               Scheduler uiScheduler) {
        this.interactor = interactor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showIdle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onEmptyFolderStackArrived() {
        getViewState().showProgress();
        interactor.getCurrentFolderScreens()
                .observeOn(uiScheduler)
                .subscribe(this::onScreensReceived, this::onScreensReceivingError);
    }

    private void onScreensReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showError(errorCommand);
    }

    private void onScreensReceived(List<String> paths) {
        getViewState().showIdle();
        getViewState().showFolderScreens(paths);
    }
}
