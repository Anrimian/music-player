package com.github.anrimian.musicplayer.domain.utils.rx

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

class CacheFlowable<T : Any>(
    flowable: Flowable<T>,
    private val noCacheItem: T
) {

    private val resetCacheSubject = PublishProcessor.create<T>()

    private val observable = flowable.mergeWith(resetCacheSubject)
        .replay(1)
        .refCount()
        .filter { item -> item !== noCacheItem }

    fun getFlowable(): Flowable<T> = observable

    fun clearCache() {
        resetCacheSubject.onNext(noCacheItem)
    }

}