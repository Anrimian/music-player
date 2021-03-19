package com.github.anrimian.musicplayer.ui.common.mvp

import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.utils.moxy.RxMvpPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import moxy.MvpView

abstract class AppPresenter<T : MvpView>(
        protected val uiScheduler: Scheduler,
        protected val errorParser: ErrorParser
): RxMvpPresenter<T>() {

    protected fun <K> Observable<K>.subscribeOnUi(onNext: (K) -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
                .subscribe(onNext, onError)
                .autoDispose()
    }

    protected fun <K> Observable<K>.subscribeOnUi(onNext: (K) -> Unit,
                                                  onError: (Throwable) -> Unit,
                                                  onComplete: () -> Unit) {
        this.observeOn(uiScheduler)
                .subscribe(onNext, onError, onComplete)
                .autoDispose()
    }

    protected fun <K> Observable<K>.unsafeSubscribeOnUi(onNext: (K) -> Unit) {
        this.observeOn(uiScheduler)
                .subscribe(onNext)
                .autoDispose()
    }

    protected fun <K> Single<K>.subscribeOnUi(onNext: (K) -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
                .subscribe(onNext, onError)
                .ignoreDisposable()
    }

    protected fun Completable.subscribeOnUi(onNext: () -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
                .subscribe(onNext, onError)
                .ignoreDisposable()
    }

    protected fun Completable.justSubscribe(onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
                .subscribe({}, onError)
                .ignoreDisposable()
    }

    protected fun Completable.unsafeSubscribeOnUi(onComplete: () -> Unit) {
        this.observeOn(uiScheduler)
                .subscribe(onComplete)
                .ignoreDisposable()
    }

}