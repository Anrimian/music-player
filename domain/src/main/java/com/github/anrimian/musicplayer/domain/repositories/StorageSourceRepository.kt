package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

interface StorageSourceRepository {

    /**
     * completes if there are no info about file in database
     */
    fun getStorageSource(
        compositionId: Long
    ): Maybe<CompositionContentSource>

    fun getStorageSources(
        compositionIds: List<Long>
    ): Single<Map<Long, CompositionContentSource>>

    fun getExternalStorageSource(
        composition: CompositionSource
    ): Single<CompositionContentSource>

    fun getCompositionArtworkBinaryData(compositionId: Long): Maybe<ByteArray>

    fun getCompositionUri(compositionId: Long): String
}