package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import java.io.FileDescriptor

interface StorageSourceRepository {

    fun getStorageSource(
        compositionId: Long
    ): Maybe<CompositionContentSource>

    fun getExternalStorageSource(
        composition: CompositionSource
    ): Single<CompositionContentSource>

    fun getCompositionArtworkBinaryData(compositionId: Long): Maybe<ByteArray>

    fun getCompositionFileDescriptor(compositionId: Long): FileDescriptor
}