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

/**
 * Util to fix kotlin casting issues
 */
inline fun <U : Any, T : Any> Observable<T>.collectIntoList(
    crossinline collector: (ArrayList<U>, T) -> Unit
): Single<List<U>> {
    return collectInto(ArrayList<U>() as List<U>, { c, i -> collector(c as ArrayList<U> , i) })
}

fun <T : Any> Observable<List<T>>.firstListItemOrComplete(): Observable<T> {
    return takeWhile { list -> list.isNotEmpty() }.map { list -> list[0] }
}

fun <T : Any> Observable<T>.attachGateObservable(
    gateObservable: Observable<Boolean>
): Observable<T> {
    var lastBlockedItem: T? = null
    return Observable.combineLatest(
        this,
        gateObservable,
        { source, isGateBlocked ->
            Observable.create { emitter ->
                if (isGateBlocked) {
                    lastBlockedItem = source
                } else {
                    if (lastBlockedItem != null) {
                        emitter.onNext(lastBlockedItem!!)
                        lastBlockedItem = null
                    } else {
                        emitter.onNext(source)
                    }
                }
            }
        }
    ).flatMap { o -> o }
}