package com.github.anrimian.musicplayer.ui.common.mvp

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.utils.moxy.RxMvpPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import moxy.MvpView

/**
 * - name1 (subscribe, ignore success case, and transform throwable)
 * - name2 (subscribe on ui scheduler, add to presenter disposable)
 * - name3 (subscribe on ui scheduler, add to presenter disposable, transform throwable)
 * - unsafeSubscribeOnUi ((..onUi)subscribe on ui scheduler, (...subscribe...)add to presenter disposable, (unsafe...)ignore error case)
 * - name5 (subscribe on ui scheduler, add to presenter disposable, ()ignore success case)
 * - name6 (subscribe on ui scheduler, add to presenter disposable, ()ignore success case, (..)and transform throwable)
 * - launch... (subscribe on ui scheduler, (launch..)do not add to presenter disposable)
 * - launch... (subscribe on ui scheduler, (launch..)do not add to presenter disposable and (...WE??)transform throwable),
 */
abstract class AppPresenter<T : MvpView>(
    protected val uiScheduler: Scheduler,
    protected val errorParser: ErrorParser
): RxMvpPresenter<T>() {

    //--Observable

    protected fun <K: Any> Observable<K>.subscribeOnUi(onNext: (K) -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError)
            .autoDispose()
    }

    protected fun <K: Any> Observable<K>.subscribeOnUi(onNext: (K) -> Unit,
                                                       onError: (Throwable) -> Unit,
                                                       onComplete: () -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError, onComplete)
            .autoDispose()
    }

    protected fun <K: Any> Observable<K>.unsafeSubscribeOnUi(onNext: (K) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext)
            .autoDispose()
    }

    //--Single

    protected fun <K: Any> Single<K>.subscribeOnUi(onNext: (K) -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError)
            .ignoreDisposable()
    }

    protected fun <K: Any> Single<K>.launchOnUi(onNext: (K) -> Unit, onError: (ErrorCommand) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext) { t -> onError(errorParser.parseError(t)) }
            .ignoreDisposable()
    }

    protected fun <K: Any> Single<K>.runOnUi(onNext: (K) -> Unit, onError: (ErrorCommand) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, { t -> onError(errorParser.parseError(t)) }, presenterDisposable)
    }

    protected fun <K: Any> Single<K>.unsafeSubscribeOnUi(onNext: (K) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext)
            .ignoreDisposable()
    }

    //--Completable

    protected fun Completable.subscribeOnUi(onNext: () -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError)
            .ignoreDisposable()
    }

    protected fun Completable.justSubscribeOnUi(onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe({}, onError)
            .autoDispose()
    }

    protected fun Completable.subscribe(
        onComplete: () -> Unit,
        onError: (ErrorCommand) -> Unit
    ): Disposable {
        return subscribe(onComplete, { t -> onError(errorParser.parseError(t)) }, presenterDisposable)
    }

    protected fun Completable.subscribe(onError: (ErrorCommand) -> Unit): Disposable {
        return subscribe({}, { t -> onError(errorParser.parseError(t)) }, presenterDisposable)
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