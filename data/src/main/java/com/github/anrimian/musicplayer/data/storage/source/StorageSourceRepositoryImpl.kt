package com.github.anrimian.musicplayer.data.storage.source

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.models.composition.source.UriContentSource
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.FileReadTimeoutException
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

private const val STORAGE_TIMEOUT_SECONDS = 3L

class StorageSourceRepositoryImpl(
    private val compositionsDao: CompositionsDaoWrapper,
    private val systemAudioCatalogProvider: SystemAudioCatalogProvider,
    private val compositionSourceEditor: CompositionSourceEditor,
    private val scheduler: Scheduler,
): StorageSourceRepository {

    override fun getStorageSource(compositionId: Long): Maybe<CompositionContentSource> {
        return getStorageCompositionSource(compositionId)
            .timeout(STORAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS, Maybe.error(FileReadTimeoutException()))
            .subscribeOn(scheduler)
    }

    override fun getStorageSources(
        compositionIds: List<Long>
    ): Single<Map<Long, CompositionContentSource>> {
        return Single.fromCallable {
            val storageIds = compositionsDao.selectStorageIds(compositionIds)
            val result = HashMap<Long, CompositionContentSource>(storageIds.size)
            for ((id, storageId) in storageIds) {
                result[id] = StorageCompositionSource(systemAudioCatalogProvider.getCompositionUri(storageId))
            }
            result as Map<Long, CompositionContentSource>
        }.subscribeOn(scheduler)
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

    override fun getCompositionUri(compositionId: Long): String {
        val storageId = compositionsDao.requireStorageId(compositionId)
        return systemAudioCatalogProvider.getCompositionUri(storageId).toString()
    }

    private fun getStorageCompositionSource(compositionId: Long): Maybe<CompositionContentSource> {
        return compositionsDao.selectStorageId(compositionId)
            .map { id -> StorageCompositionSource(systemAudioCatalogProvider.getCompositionUri(id)) }
    }
}