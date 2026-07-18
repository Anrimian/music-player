package com.github.anrimian.musicplayer.ui.common.mvp

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.utils.moxy.RxMvpPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import moxy.MvpView
import moxy.presenterScope
import com.github.anrimian.musicplayer.domain.utils.coroutines.launchCatching as launchCatchingExt
import com.github.anrimian.musicplayer.domain.utils.coroutines.subscribeCatching as subscribeCatchingExt

abstract class AppPresenter<T : MvpView>(
    protected val uiScheduler: Scheduler,
    protected val errorParser: ErrorParser
): RxMvpPresenter<T>() {

    protected fun launch(
        onError: ((ErrorCommand) -> Unit)? = null,
        onProgress: ((Boolean) -> Unit)? = null,
        scope: CoroutineScope = presenterScope,
        action: (suspend CoroutineScope.() -> Unit)
    ): Job {
        return scope.launchCatchingExt(
            onError = onError?.let { handler -> { t -> handler(errorParser.parseError(t)) } },
            onProgress,
            action
        )
    }

    protected fun <T> Flow<T>.subscribe(
        scope: CoroutineScope = presenterScope,
        onError: ((ErrorCommand) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onProgress: ((Boolean) -> Unit)? = null,
        onNext: ((T) -> Unit)? = null
    ): Job {
        return subscribeCatching(
            onNext = onNext,
            onError = onError?.let { handler -> { t -> handler(errorParser.parseError(t)) } },
            onComplete = onComplete,
            onProgress = onProgress,
            scope = scope
        )
    }

    protected fun <T> Flow<T>.subscribeCatching(
        scope: CoroutineScope = presenterScope,
        onError: ((Throwable) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onProgress: ((Boolean) -> Unit)? = null,
        onNext: ((T) -> Unit)? = null,
    ): Job {
        return subscribeCatchingExt(
            onNext = onNext,
            onError = onError,
            onComplete = onComplete,
            onProgress = onProgress,
            scope = scope
        )
    }

    //--Observable

    protected fun <K: Any> Observable<K>.runOnUi(
        onNext: (K) -> Unit,
        onError: (ErrorCommand) -> Unit,
        onComplete: () -> Unit
    ) {
        this.subscribeOnUi(onNext, { t -> onError(errorParser.parseError(t)) }, onComplete)
    }

    protected fun <K: Any> Observable<K>.runOnUi(
        onNext: (K) -> Unit,
        onError: (ErrorCommand) -> Unit
    ) {
        this.subscribeOnUi(onNext) { t -> onError(errorParser.parseError(t)) }
    }

    protected fun <K: Any> Observable<K>.subscribeOnUi(
        onNext: (K) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError)
            .autoDispose()
    }

    protected fun <K: Any> Observable<K>.subscribeOnUi(
        onNext: (K) -> Unit,
        onError: (Throwable) -> Unit,
        onComplete: () -> Unit
    ) {
        this.observeOn(uiScheduler).subscribe(onNext, onError, onComplete, presenterDisposable)
    }

    protected fun <K: Any> Observable<K>.unsafeSubscribeOnUi(onNext: (K) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext)
            .autoDispose()
    }

    //--Flowable

    protected fun <K: Any> Flowable<K>.runOnUi(
        onNext: (K) -> Unit,
        onError: (ErrorCommand) -> Unit
    ) {
        this.subscribeOnUi(onNext) { t -> onError(errorParser.parseError(t)) }
    }

    protected fun <K: Any> Flowable<K>.subscribeOnUi(
        onNext: (K) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError)
            .autoDispose()
    }

    //--Single

    protected fun <K: Any> Single<K>.runOnUi(onNext: (K) -> Unit, onError: (ErrorCommand) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, { t -> onError(errorParser.parseError(t)) }, presenterDisposable)
    }

    protected fun <K: Any> Single<K>.justSubscribeOnUi(onNext: (K) -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError, presenterDisposable)
    }

    protected fun <K: Any> Single<K>.launchOnUi(onNext: (K) -> Unit, onError: (ErrorCommand) -> Unit) {
        this.subscribeOnUi(onNext) { t -> onError(errorParser.parseError(t)) }
    }

    protected fun <K: Any> Single<K>.subscribeOnUi(onNext: (K) -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError)
            .ignoreDisposable()
    }

    protected fun <K: Any> Single<K>.unsafeSubscribeOnUi(onNext: (K) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext)
            .ignoreDisposable()
    }

    //--Maybe

    protected fun <K: Any> Maybe<K>.runOnUi(onNext: (K?) -> Unit, onError: (ErrorCommand) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, { t -> onError(errorParser.parseError(t)) }, { onNext(null) }, presenterDisposable)
    }

    //--Completable

    protected fun Completable.subscribeOnUi(onNext: () -> Unit, onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe(onNext, onError)
            .ignoreDisposable()
    }

    protected fun Completable.launchOnUi(onNext: () -> Unit, onError: (ErrorCommand) -> Unit) {
        this.subscribeOnUi(onNext) { t -> onError(errorParser.parseError(t)) }
    }

    protected fun Completable.launchOnUi(onError: (ErrorCommand) -> Unit) {
        this.launchOnUi({}, onError)
    }

    protected fun Completable.justSubscribeOnUi(onError: (Throwable) -> Unit) {
        this.observeOn(uiScheduler)
            .subscribe({}, onError, presenterDisposable)
    }

    protected fun Completable.runOnUi(onError: (ErrorCommand) -> Unit) {
        this.justSubscribeOnUi { t -> onError(errorParser.parseError(t)) }
    }

    protected fun Completable.subscribe(
        onComplete: () -> Unit,
        onError: (ErrorCommand) -> Unit
    ): Disposable {
        return observeOn(uiScheduler)
            .subscribe(onComplete, { t -> onError(errorParser.parseError(t)) }, presenterDisposable)
    }

    protected fun Completable.subscribe(onError: (ErrorCommand) -> Unit): Disposable {
        return subscribe({}, { t -> onError(errorParser.parseError(t)) }, presenterDisposable)
    }

    protected fun Completable.justRunOnUi(onError: (ErrorCommand) -> Unit) {
        this.justSubscribe { t -> onError(errorParser.parseError(t)) }
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