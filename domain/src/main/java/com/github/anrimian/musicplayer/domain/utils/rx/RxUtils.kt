package com.github.anrimian.musicplayer.domain.utils.rx

import io.reactivex.rxjava3.core.Observable

fun <T: Any> Observable<T>.doOnFirst(action: (T) -> Unit): Observable<T> {
    return take(1)
        .doOnNext { action.invoke(it) }
        .concatWith(skip(1))
}