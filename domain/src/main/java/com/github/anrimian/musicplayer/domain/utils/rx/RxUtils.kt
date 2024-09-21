package com.github.anrimian.musicplayer.domain.utils.rx

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

fun <T: Any> Observable<T>.doOnFirst(action: (T) -> Unit): Observable<T> {
    return take(1)
        .doOnNext { action.invoke(it) }
        .concatWith(skip(1))
}

fun <T : Any> Observable<T>.doOnEvent(completableAction: (T) -> Completable?): Observable<T> {
    return flatMapSingle { o ->
        val s = Single.just(o)
        return@flatMapSingle completableAction(o)?.andThen(s) ?: s
    }
}

fun <T: Any> BehaviorSubject<T>.withDefaultValue(creator: Single<T>): Observable<T> {
    return Observable.create<T> { emitter ->
        if (!this.hasValue()) {
            val d = creator.subscribe(this::onNext, emitter::onError)
            emitter.setDisposable(d)
        }
    }.mergeWith(this)
}

/**
 * Never intended to be unsubscribed
 */
class LazyBehaviorSubject<T : Any>(
    defaultValuesObservable: Observable<T>,
) {

    private val subject = BehaviorSubject.create<T>()
    private val observable = Observable.create<T> { emitter ->
        if (!subject.hasValue()) {
            val d = defaultValuesObservable.subscribe(this::onNext, emitter::onError)
            emitter.setDisposable(d)
        }
    }.mergeWith(subject)
        .replay(1)
        .autoConnect()

    fun getObservable() = observable
    fun onNext(value: T) = subject.onNext(value)
    fun getValue(defaultValue: T) = observable.first(defaultValue)
}