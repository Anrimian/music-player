package com.github.anrimian.musicplayer.data.utils.rx

import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.retryWithDelay(
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