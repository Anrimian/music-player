package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.RemoteCompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CompositionSourceInteractor(
    private val storageSourceRepository: StorageSourceRepository,
    private val syncInteractor: SyncInteractor<*, *, Long>
) {

    fun getCompositionSource(source: CompositionSource): Single<CompositionContentSource> {
        if (source is LibraryCompositionSource) {
            return getLibraryCompositionSource(source.composition.id)
        }
        return storageSourceRepository.getExternalStorageSource(source)
    }

    fun getLibraryCompositionSources(
        compositions: Iterable<Long>,
        currentFileIdSubject: BehaviorSubject<Long>? = null
    ): Single<ArrayList<CompositionContentSource>> {
        return Observable.fromIterable(compositions)
            .concatMapSingle { id -> getLibraryCompositionSource(id)
                .doOnSubscribe { currentFileIdSubject?.onNext(id) }
            }
            .collect(::ArrayList, ArrayList<CompositionContentSource>::add)
    }

    fun getLibraryCompositionSource(compositionId: Long): Single<CompositionContentSource> {
        return storageSourceRepository.getStorageSource(compositionId)
            .switchIfEmpty(
                syncInteractor.requestFileSource(compositionId).map(::RemoteCompositionSource)
            )
    }

}