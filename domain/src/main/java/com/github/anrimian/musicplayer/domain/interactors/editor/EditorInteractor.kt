package com.github.anrimian.musicplayer.domain.interactors.editor

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.image.ImageSource
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.toKeyPair
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

class EditorInteractor(
    private val sourceInteractor: CompositionSourceInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    private val editorRepository: EditorRepository,
    private val libraryRepository: LibraryRepository,
    private val storageSourceRepository: StorageSourceRepository,
) {

    fun changeCompositionGenre(
        compositionId: Long,
        oldGenre: String,
        newGenre: String,
    ): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionGenre(compositionId, source, oldGenre, newGenre)
        }
    }

    fun addCompositionGenre(compositionId: Long, newGenre: String): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.addCompositionGenre(compositionId, source, newGenre)
        }
    }

    fun moveGenre(compositionId: Long, from: Int, to: Int): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.moveGenre(compositionId, source, from, to)
        }
    }

    fun removeCompositionGenre(compositionId: Long, genre: String): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.removeCompositionGenre(compositionId, source, genre)
        }
    }

    fun restoreRemovedCompositionGenre(): Completable {
        return editorRepository.restoreRemovedCompositionGenre()
    }

    fun editCompositionAuthor(compositionId: Long, newAuthor: String?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionAuthor(compositionId, source, newAuthor)
        }
    }

    fun editCompositionAlbum(compositionId: Long, newAlbum: String?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionAlbum(compositionId, source, newAlbum)
        }
    }

    fun editCompositionAlbumArtist(compositionId: Long, newArtist: String?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionAlbumArtist(compositionId, source, newArtist)
        }
    }

    fun editCompositionLyrics(compositionId: Long, text: String?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionLyrics(compositionId, source, text)
        }
    }

    fun editCompositionTitle(compositionId: Long, newTitle: String): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionTitle(compositionId, source, newTitle)
        }
    }

    fun editCompositionTrackNumber(compositionId: Long, trackNumber: Long?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionTrackNumber(compositionId, source, trackNumber.nullIfZero())
        }
    }

    fun editCompositionDiscNumber(compositionId: Long, discNumber: Long?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionDiscNumber(compositionId, source, discNumber.nullIfZero())
        }
    }

    fun editCompositionComment(compositionId: Long, comment: String?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionComment(compositionId, source, comment)
        }
    }

    fun editCompositionFileName(compositionId: Long, newFileName: String): Completable {
        return editorRepository.changeCompositionFileName(compositionId, newFileName)
            .flatMapCompletable { path -> syncInteractor.onLocalFileKeyChanged(path.toKeyPair()) }
    }

    fun getCompositionObservable(id: Long): Observable<FullComposition> {
        return libraryRepository.getFullCompositionObservable(id)
    }

    fun getAlbumObservable(albumId: Long): Observable<Album> {
        return libraryRepository.getAlbumObservable(albumId)
    }

    fun updateTagsFromSource(fullComposition: FullComposition): Completable {
        return storageSourceRepository.getStorageSource(fullComposition.id)
            .flatMapCompletable { source -> editorRepository.updateTagsFromSource(source, fullComposition) }
    }

    fun updateAlbumName(
        name: String?,
        albumId: Long,
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>,
    ): Completable {
        return runBatchEditAction(
            libraryRepository.getCompositionIdsInAlbum(albumId),
            downloadingSubject
        ) { ids, sources ->
            editorRepository.updateAlbumName(name, ids, sources, albumId, editingSubject)
        }
    }

    fun updateAlbumArtist(
        name: String?,
        albumId: Long,
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>,
    ): Completable {
        return runBatchEditAction(
            libraryRepository.getCompositionIdsInAlbum(albumId),
            downloadingSubject
        ) { ids, sources ->
            editorRepository.updateAlbumArtist(name, ids, sources, albumId, editingSubject)
        }
    }

    fun updateArtistName(
        name: String?,
        artistId: Long,
        affectedFilesCount: (Int) -> Unit,
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        return runBatchEditAction(
            libraryRepository.getAllCompositionIdsByArtists(artistId)
                .doOnSuccess { ids -> affectedFilesCount(ids.size) },
            downloadingSubject
        ) { ids, sources ->
            editorRepository.updateArtistName(name, ids, sources, artistId, editingSubject)
        }
    }

    fun updateGenreName(
        name: String,
        genreId: Long,
        affectedFilesCount: (Int) -> Unit,
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        return runBatchEditAction(
            libraryRepository.getAllCompositionsByGenre(genreId)
                .doOnSuccess { ids -> affectedFilesCount(ids.size) },
            downloadingSubject
        ) { ids, sources ->
            editorRepository.updateGenreName(name, ids, sources, genreId, editingSubject)
        }
    }

    fun getAuthorNames() = libraryRepository.authorNames

    fun getAlbumNames() = libraryRepository.albumNames

    fun getGenreNames(forCompositionId: Long) = libraryRepository.getGenreNames(forCompositionId)

    fun changeCompositionAlbumArt(compositionId: Long, imageSource: ImageSource?): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.changeCompositionAlbumArt(compositionId, source, imageSource)
        }
    }

    fun removeCompositionAlbumArt(compositionId: Long): Completable {
        return runEditAction(compositionId) { source ->
            editorRepository.removeCompositionAlbumArt(compositionId, source)
        }
    }

    fun getCompositionLyrics(compositionId: Long): Single<String> {
        return libraryRepository.getLyricsObservable(compositionId)
            .firstOrError()
    }

    private fun runEditAction(compositionId: Long, action: (CompositionContentSource) -> Completable): Completable {
        return sourceInteractor.getLibraryCompositionSource(compositionId)
            .flatMapCompletable { source -> action(source) }
            .doOnComplete { syncInteractor.notifyLocalFileChanged() }
    }

    private fun runBatchEditAction(
        idsSingle: Single<List<Long>>,
        downloadingSubject: BehaviorSubject<Long>,
        updater: (ids: List<Long>, sources: List<CompositionContentSource>) -> Completable
    ): Completable {
        return idsSingle.flatMapCompletable { ids ->
            sourceInteractor.getLibraryCompositionSources(ids, downloadingSubject)
                .doOnSuccess { downloadingSubject.onComplete() }
                .flatMapCompletable { sources -> updater(ids, sources) }
                .doOnComplete { syncInteractor.notifyLocalFileChanged() }
        }
    }

    private fun Long?.nullIfZero() = if (this != null && this == 0L) null else this
}