package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.change.ChangedCompositionPath
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.image.ImageSource
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

interface EditorRepository {
    
    fun changeCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        oldGenre: String,
        newGenre: String
    ): Completable

    fun addCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        newGenre: String
    ): Completable

    fun moveGenre(
        compositionId: Long,
        source: CompositionContentSource,
        from: Int,
        to: Int
    ): Completable

    fun removeCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        genre: String
    ): Completable

    fun restoreRemovedCompositionGenre(): Completable

    fun changeCompositionAuthor(
        compositionId: Long,
        source: CompositionContentSource,
        newAuthor: String?
    ): Completable

    fun changeCompositionAlbumArtist(
        compositionId: Long,
        source: CompositionContentSource,
        newAuthor: String?
    ): Completable

    fun changeCompositionAlbum(
        compositionId: Long,
        source: CompositionContentSource,
        newAlbum: String?
    ): Completable

    fun changeCompositionTitle(
        compositionId: Long,
        source: CompositionContentSource,
        title: String
    ): Completable

    fun changeCompositionTrackNumber(
        compositionId: Long,
        source: CompositionContentSource,
        trackNumber: Long?
    ): Completable

    fun changeCompositionDiscNumber(
        compositionId: Long,
        source: CompositionContentSource,
        discNumber: Long?
    ): Completable

    fun changeCompositionComment(
        compositionId: Long,
        source: CompositionContentSource,
        text: String?
    ): Completable

    fun changeCompositionLyrics(
        compositionId: Long,
        source: CompositionContentSource,
        text: String?
    ): Completable

    fun changeCompositionFileName(
        compositionId: Long,
        fileName: String
    ): Single<ChangedCompositionPath>

    fun changeFolderName(
        folderId: Long,
        newFolderName: String
    ): Single<List<ChangedCompositionPath>>

    fun moveFiles(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        toFolderId: Long?
    ): Single<List<ChangedCompositionPath>>

    fun moveFilesToNewDirectory(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        targetParentFolderId: Long?,
        directoryName: String?
    ): Single<List<ChangedCompositionPath>>

    fun updateAlbumName(
        name: String?,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        albumId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable

    fun updateAlbumArtist(
        artist: String?,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        albumId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable

    fun updateArtistName(
        name: String?,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        artistId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable

    fun updateGenreName(
        name: String,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        genreId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable

    fun changeCompositionAlbumArt(
        compositionId: Long,
        source: CompositionContentSource,
        imageSource: ImageSource?
    ): Completable

    fun removeCompositionAlbumArt(
        compositionId: Long,
        source: CompositionContentSource
    ): Completable

    fun updateTagsFromSource(
        source: CompositionContentSource,
        composition: FullComposition
    ): Completable

}