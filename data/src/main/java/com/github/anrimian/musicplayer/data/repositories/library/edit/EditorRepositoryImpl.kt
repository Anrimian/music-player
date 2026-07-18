package com.github.anrimian.musicplayer.data.repositories.library.edit

import android.os.Build
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.EditorTimeoutException
import com.github.anrimian.musicplayer.data.storage.exceptions.GenreAlreadyPresentException
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileTagInfo
import com.github.anrimian.musicplayer.domain.models.image.ImageSource
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class EditorRepositoryImpl(
    private val sourceEditor: CompositionSourceEditor,
    private val compositionsDao: CompositionsDaoWrapper,
    private val albumsDao: AlbumsDaoWrapper,
    private val artistsDao: ArtistsDaoWrapper,
    private val genresDao: GenresDaoWrapper,
    private val systemAudioCatalogProvider: SystemAudioCatalogProvider,
    private val scheduler: Scheduler
) : EditorRepository {

    private var removedGenreName: String? = null
    private var removedGenreCompositionId: Long = 0
    private var removedGenreCompositionSource: CompositionContentSource? = null
    private var removedGenrePosition = 0

    override fun changeCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        oldGenre: String,
        newGenre: String
    ): Completable {
        val action = Completable.fromAction {
            if (genresDao.containsCompositionGenre(compositionId, newGenre)) {
                throw GenreAlreadyPresentException()
            }
        }.andThen(sourceEditor.changeCompositionGenre(source, oldGenre, newGenre))
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                genresDao.changeCompositionGenre(compositionId, oldGenre, newGenre)
            }
        return performSourceUpdate(source, action)
    }

    override fun addCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        newGenre: String
    ): Completable {
        val action = Completable.fromAction {
            if (genresDao.containsCompositionGenre(compositionId, newGenre)) {
                throw GenreAlreadyPresentException()
            }
        }.andThen(sourceEditor.addCompositionGenre(source, newGenre))
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                genresDao.addCompositionToGenre(compositionId, newGenre)
            }
        return performSourceUpdate(source, action)
    }

    override fun moveGenre(
        compositionId: Long,
        source: CompositionContentSource,
        from: Int,
        to: Int
    ): Completable {
        return performSourceUpdate(source, sourceEditor.moveGenre(source, from, to)
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                genresDao.moveGenres(compositionId, from, to)
            }
        )
    }

    override fun removeCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        genre: String
    ): Completable {
        return performSourceUpdate(source, sourceEditor.removeCompositionGenre(source, genre)
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                removedGenreName = genre
                removedGenreCompositionId = compositionId
                removedGenreCompositionSource = source
                removedGenrePosition = genresDao.removeCompositionFromGenre(compositionId, genre)
            }
        )
    }

    override fun restoreRemovedCompositionGenre(): Completable {
        return Single.fromCallable {
            val name = removedGenreName ?: return@fromCallable Completable.complete()
            val id = removedGenreCompositionId
            val source = removedGenreCompositionSource!!
            val position = removedGenrePosition
            val action = Completable.fromAction {
                if (genresDao.containsCompositionGenre(id, name)) {
                    throw GenreAlreadyPresentException()
                }
            }.andThen(sourceEditor.addCompositionGenre(source, name, position))
                .doOnComplete { genresDao.addCompositionToGenre(id, name, position) }

            performSourceUpdate(source, action)
        }.flatMapCompletable { c -> c }
    }

    override fun changeCompositionAuthor(
        compositionId: Long,
        source: CompositionContentSource,
        newAuthor: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionAuthor(source, newAuthor)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete {
                compositionsDao.updateArtist(compositionId, newAuthor)
                compositionsDao.setModifyTimeToCurrent(compositionId)
            }
        )
    }

    override fun changeCompositionAlbumArtist(
        compositionId: Long,
        source: CompositionContentSource,
        newAuthor: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionAlbumArtist(source, newAuthor)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete {
                compositionsDao.updateAlbumArtist(compositionId, newAuthor)
                compositionsDao.setModifyTimeToCurrent(compositionId)
            }
        )
    }

    override fun changeCompositionAlbum(
        compositionId: Long,
        source: CompositionContentSource,
        newAlbum: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionAlbum(source, newAlbum)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete {
                compositionsDao.updateAlbum(compositionId, newAlbum)
                compositionsDao.setModifyTimeToCurrent(compositionId)
            }
        )
    }

    override fun changeCompositionTitle(
        compositionId: Long,
        source: CompositionContentSource,
        title: String
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionTitle(source, title)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateTitle(compositionId, title) }
        )
    }

    override fun changeCompositionTrackNumber(
        compositionId: Long,
        source: CompositionContentSource,
        trackNumber: Long?
    ): Completable {
        return performSourceUpdate(source,
            sourceEditor.setCompositionTrackNumber(source, trackNumber)
                .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
                .doOnComplete { compositionsDao.updateTrackNumber(compositionId, trackNumber) }
        )
    }

    override fun changeCompositionDiscNumber(
        compositionId: Long,
        source: CompositionContentSource,
        discNumber: Long?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionDiscNumber(source, discNumber)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateDiscNumber(compositionId, discNumber) }
        )
    }

    override fun changeCompositionComment(
        compositionId: Long,
        source: CompositionContentSource,
        text: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionComment(source, text)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateComment(compositionId, text) }
        )
    }

    override fun changeCompositionLyrics(
        compositionId: Long,
        source: CompositionContentSource,
        text: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionLyrics(source, text)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateLyrics(compositionId, text) }
        )
    }

    override fun updateAlbumName(
        name: String,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        albumId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = sourceEditor.setCompositionsAlbum(sources, name, editingSubject)
            .doOnComplete { albumsDao.updateAlbumName(albumId, name) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun updateAlbumArtist(
        artist: String?,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        albumId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = sourceEditor.setCompositionsAlbumArtist(sources, artist, editingSubject)
            .doOnComplete { albumsDao.updateAlbumArtist(albumId, artist) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun updateArtistName(
        name: String,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        artistId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = Single.fromCallable { artistsDao.getAuthorName(artistId) }
            .flatMapCompletable { oldName ->
                sourceEditor.renameCompositionsAuthor(sources, oldName, name, editingSubject)
            }
            .doOnComplete { artistsDao.updateArtistName(artistId, name) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun updateGenreName(
        name: String,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        genreId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = Single.fromCallable { genresDao.getGenreName(genreId) }
            .flatMapCompletable { oldName ->
                sourceEditor.setCompositionsGenre(sources, oldName, name, editingSubject)
            }
            .doOnComplete { genresDao.updateGenreName(name, genreId, compositionIds) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun changeCompositionAlbumArt(
        compositionId: Long,
        source: CompositionContentSource,
        imageSource: ImageSource?
    ): Completable {
        val action = sourceEditor.changeCompositionAlbumArt(source, imageSource)
            .doOnSuccess { newSize ->
                setCompositionInitialSourceToApp(compositionId)
                compositionsDao.updateCoverModifyTimeAndSize(compositionId, newSize, System.currentTimeMillis())
            }
            .ignoreElement()
            .timeout(
                CHANGE_COVER_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS,
                Completable.error(EditorTimeoutException())
            )
        return performSourceUpdate(source, action)
    }

    override fun removeCompositionAlbumArt(
        compositionId: Long,
        source: CompositionContentSource
    ): Completable {
        return performSourceUpdate(source, sourceEditor.removeCompositionAlbumArt(source)
            .doOnSuccess { newSize ->
                setCompositionInitialSourceToApp(compositionId)
                compositionsDao.updateCoverModifyTimeAndSize(compositionId, newSize, System.currentTimeMillis())
                runSystemRescan(source)
            }
            .ignoreElement()
            .timeout(
                CHANGE_COVER_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS,
                Completable.error(EditorTimeoutException())
            )
        )
    }

    /**
     * Album-artist in android system and in common file has conflicts. This function
     * updates media library by real file source tags.
     */
    override fun updateTagsFromSource(
        source: CompositionContentSource,
        composition: FullComposition
    ): Completable {
        return sourceEditor.getAudioFileInfo(source)
            .flatMapCompletable { info -> updateCompositionTags(composition, info) }
            .doOnComplete { setCompositionInitialSourceToApp(composition.id) }
            .subscribeOn(scheduler)
    }

    private fun updateCompositionTags(
        composition: FullComposition,
        fileInfo: AudioFileTagInfo
    ): Completable {
        return Completable.fromAction {
            compositionsDao.updateCompositionByFileInfo(composition, fileInfo)
        }
    }

    private fun performSourceUpdate(
        source: CompositionContentSource?,
        completable: Completable
    ): Completable {
        return completable
            .doOnSubscribe { systemAudioCatalogProvider.setContentObserverEnabled(false) }
            .doOnComplete { runSystemRescan(source) }
            .doFinally { systemAudioCatalogProvider.setContentObserverEnabled(true) }
            .subscribeOn(scheduler)
    }

    private fun performSourceUpdate(
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        completable: Completable
    ): Completable {
        return completable
            .doOnSubscribe { systemAudioCatalogProvider.setContentObserverEnabled(false) }
            .doOnComplete {
                setCompositionIdsInitialSourceToApp(compositionIds)
                runSystemRescan(sources)
            }
            .doFinally { systemAudioCatalogProvider.setContentObserverEnabled(true) }
            .subscribeOn(scheduler)
    }

    private fun runSystemRescan(sources: List<CompositionContentSource>) {
        for (source in sources) {
            runSystemRescan(source)
        }
    }

    private fun runSystemRescan(source: CompositionContentSource?) {
        if (source is StorageCompositionSource) {
            val (uri) = source
            systemAudioCatalogProvider.scanMedia(uri)
        }
    }

    /**
     * Set initial source to app to display in-app delete dialog
     */
    private fun setCompositionInitialSourceToApp(id: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            compositionsDao.updateCompositionInitialSource(
                id,
                InitialSource.APP,
                InitialSource.LOCAL
            )
        }
    }

    private fun setCompositionIdsInitialSourceToApp(compositionIds: List<Long>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            compositionsDao.updateCompositionIdsInitialSource(
                compositionIds,
                InitialSource.APP,
                InitialSource.LOCAL
            )
        }
    }

    companion object {
        private const val CHANGE_COVER_TIMEOUT_MILLIS: Long = 25000
    }

}