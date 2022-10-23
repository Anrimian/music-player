package com.github.anrimian.musicplayer.data.utils.rx

import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

fun <T: Any> Observable<T>.retryWithDelay(
    maxRetryCount: Int,
    delay: Long,
    unit: TimeUnit
): Observable<T> {
    var retryCount = 0
    return retryWhen { observable ->
        observable.flatMap { throwable ->
            if (++retryCount < maxRetryCount) {
                Observable.timer(delay, unit)
            } else {
                Observable.error(throwable)
            }
        }
    }
}

fun <T : Any> Observable<T>.mapError(mapper: (Throwable) -> Throwable): Observable<T> {
    return onErrorResumeNext { t -> Observable.error(mapper(t)) }
}

fun <T : Any> Single<T>.mapError(mapper: (Throwable) -> Throwable): Single<T> {
    return onErrorResumeNext { t -> Single.error(mapper(t)) }
}

fun <T> Maybe<T>.mapError(mapper: (Throwable) -> Throwable): Maybe<T> {
    return onErrorResumeNext { t -> Maybe.error(mapper(t)) }
}

fun Completable.mapError(mapper: (Throwable) -> Throwable): Completable {
    return onErrorResumeNext { t -> Completable.error(mapper(t)) }
}

fun <T> Observable<List<T>>.takeFirstListItem(): Observable<Optional<T>> {
    return map { list -> Optional(list.firstOrNull()) }
}