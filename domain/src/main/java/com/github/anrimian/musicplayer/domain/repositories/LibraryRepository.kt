package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.folders.AbstractDirectory
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderInfo
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.search.CompositionLookup
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Created on 24.10.2017.
 */
interface LibraryRepository {

    //compositions
    fun getAllCompositionsObservable(searchText: String?): Observable<List<Composition>>

    fun getCompositionObservable(id: Long): Observable<Composition>

    fun getFullCompositionObservable(id: Long): Observable<FullComposition>

    fun getLyricsObservable(id: Long): Observable<String>

    fun writeErrorAboutComposition(
        corruptionType: CorruptionType?,
        composition: Composition
    ): Completable

    fun deleteComposition(composition: Composition): Single<DeletedComposition>

    fun deleteCompositions(compositions: List<CompositionModel>): Single<List<DeletedComposition>>

    fun getMissingCompositionsCountObservable(): Observable<Int>

    fun getMissingAudioFilesObservable(): Observable<List<AudioFileInfo>>

    fun deleteMissingCompositions(): Single<List<DeletedComposition>>

    fun getCompositionKeys(lookup: CompositionLookup): Single<List<FileKey>>

    //folders
    fun getFoldersInFolder(
        folderId: Long?,
        searchQuery: String?
    ): Observable<List<FileSource>>

    fun getFolderObservable(folderId: Long): Observable<FolderInfo>

    fun getVolumes(): Observable<List<Volume>>

    fun getAllCompositionsInFolder(folderId: Long?): Single<List<Composition>>

    fun getAllCompositionsInFolders(fileSources: Iterable<FileSource>): Single<List<Composition>>

    fun deleteFolder(folder: FolderFileSource): Single<List<DeletedComposition>>

    fun deleteFolders(folders: List<FileSource>): Single<List<DeletedComposition>>

    fun getAllParentFolders(folderId: Long?): Single<List<Long>>

    fun getAllParentFoldersForComposition(compositionId: Long): Single<List<Long>>

    //artists
    fun getArtistsObservable(searchText: String?): Observable<List<Artist>>

    fun getAllCompositionIdsByArtists(artistId: Long): Single<List<Long>>

    fun getAllCompositionIdsByArtists(artists: Iterable<Artist>): Single<List<Long>>

    fun getAllCompositionsByArtists(artists: Iterable<Artist>): Single<List<Composition>>

    fun getAllCompositionsByArtistIds(artists: Iterable<Long>): Single<List<Composition>>

    fun getCompositionsByArtist(artistId: Long): Observable<List<Composition>>

    fun getArtistObservable(artistId: Long): Observable<Artist>

    fun getAllAlbumsForArtist(artistId: Long): Observable<List<Album>>

    fun getAuthorNames(): Single<Array<String>>

    //albums
    fun getAlbumsObservable(searchText: String?): Observable<List<Album>>

    fun getAlbumItemsObservable(albumId: Long): Observable<List<AlbumComposition>>

    fun getCompositionIdsInAlbum(albumId: Long): Single<List<Long>>

    fun getCompositionIdsInAlbums(albums: Iterable<Album>): Single<List<Long>>

    fun getCompositionsInAlbums(albums: Iterable<Album>): Single<List<Composition>>

    fun getCompositionsByAlbumIds(albumIds: Iterable<Long>): Single<List<Composition>>

    fun getAlbumObservable(albumId: Long): Observable<Album>

    fun getAlbumNames(): Single<Array<String>>

    //genres
    fun getGenresObservable(searchText: String?): Observable<List<Genre>>

    fun getGenreItemsObservable(genreId: Long): Observable<List<Composition>>

    fun getCompositionIdsInGenres(genres: Iterable<Genre>): Single<List<Long>>

    fun getCompositionsInGenres(genres: Iterable<Genre>): Single<List<Composition>>

    fun getCompositionsInGenresIds(genresIds: Iterable<Long>): Single<List<Composition>>

    fun getAllCompositionsByGenre(genreId: Long): Single<List<Long>>

    fun getGenreNames(forCompositionId: Long): Single<Array<String>>

    fun getGenreObservable(genreId: Long): Observable<Genre>

    //ignored folders
    fun addFolderToIgnore(dir: AbstractDirectory): Single<Pair<IgnoredFolder, List<FileKey>>>

    fun addFolderToIgnore(folder: IgnoredFolder): Single<List<FileKey>>

    fun getIgnoredFoldersObservable(): Observable<List<IgnoredFolder>>

    fun deleteIgnoredFolder(path: String): Single<List<FileKey>>

}
