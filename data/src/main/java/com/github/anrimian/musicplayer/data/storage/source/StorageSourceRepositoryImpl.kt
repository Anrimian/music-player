package com.github.anrimian.musicplayer.data.storage.source

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.models.composition.source.UriContentSource
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.io.FileDescriptor
import java.util.concurrent.TimeUnit

private const val STORAGE_TIMEOUT_SECONDS = 3L

class StorageSourceRepositoryImpl(
    private val compositionsDao: CompositionsDaoWrapper,
    private val storageMusicProvider: StorageMusicProvider,
    private val compositionSourceEditor: CompositionSourceEditor,
    private val scheduler: Scheduler,
): StorageSourceRepository {

    override fun getStorageSource(compositionId: Long): Maybe<CompositionContentSource> {
        return getStorageCompositionSource(compositionId)
            .timeout(STORAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .subscribeOn(scheduler)
    }

    override fun getExternalStorageSource(
        composition: CompositionSource,
    ): Single<CompositionContentSource> {
        return Single.fromCallable {
            if (composition is ExternalCompositionSource) {
                return@fromCallable UriContentSource(composition.uri)
            }
            throw IllegalArgumentException("unknown composition source")
        }
    }

    override fun getCompositionArtworkBinaryData(compositionId: Long): Maybe<ByteArray> {
        return getStorageCompositionSource(compositionId)
            .flatMap(compositionSourceEditor::getCompositionArtworkBinaryData)
    }

    override fun getCompositionFileDescriptor(compositionId: Long): FileDescriptor {
        val storageId = compositionsDao.getStorageId(compositionId)
        return storageMusicProvider.getFileDescriptor(storageId)
    }

    private fun getStorageCompositionSource(compositionId: Long): Maybe<CompositionContentSource> {
        return compositionsDao.selectStorageId(compositionId)
            .map { id -> StorageCompositionSource(storageMusicProvider.getCompositionUri(id)) }
    }
}