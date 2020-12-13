package com.github.anrimian.musicplayer.ui.sleep_timer;

import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import moxy.MvpPresenter;

public class SleepTimerPresenter extends MvpPresenter<SleepTimerView> {

    private final SleepTimerInteractor interactor;
    private final Scheduler scheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    public SleepTimerPresenter(SleepTimerInteractor interactor,
                               Scheduler scheduler) {
        this.interactor = interactor;
        this.scheduler = scheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showSleepTimerTime(interactor.getSleepTimerTime());
        subscribeOnSleepTimerState();
        subscribeOnSleepTimerRemainingTime();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onSleepTimerTimeChanged(long millis) {
        getViewState().showSleepTimerTime(millis);
        interactor.setSleepTimerTime(millis);
    }

    private void subscribeOnSleepTimerState() {
        presenterDisposable.add(interactor.getSleepTimerStateObservable()
                .observeOn(scheduler)
                .subscribe(getViewState()::showSleepTimerState));
    }

    private void subscribeOnSleepTimerRemainingTime() {
        presenterDisposable.add(interactor.getSleepTimerCountDownObservable()
                .observeOn(scheduler)
                .subscribe(getViewState()::showSleepRemainingSeconds));
    }
}
