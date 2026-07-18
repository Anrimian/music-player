package com.github.anrimian.musicplayer.data.repositories.library

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.data.utils.rx.retryWithDelay
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
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageScannerRepository
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.rx.collectIntoList
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.util.LinkedList
import java.util.concurrent.TimeUnit

/**
 * Created on 24.10.2017.
 */
class LibraryRepositoryImpl(
    private val storageFilesDataSource: StorageFilesDataSource,
    private val compositionsDao: CompositionsDaoWrapper,
    private val artistsDao: ArtistsDaoWrapper,
    private val albumsDao: AlbumsDaoWrapper,
    private val genresDao: GenresDaoWrapper,
    private val foldersDao: FoldersDaoWrapper,
    private val ignoredFoldersDao: IgnoredFoldersDao,
    private val settingsPreferences: SettingsRepository,
    private val storageScannerRepository: StorageScannerRepository,
    private val ioScheduler: Scheduler
) : LibraryRepository {

    override fun getAllCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return settingsPreferences.compositionsOrderObservable
            .switchMap { order ->
                settingsPreferences.displayFileNameObservable
                    .switchMap { useFileName ->
                        compositionsDao.getCompositionsObservable(order, useFileName, searchText)
                    }
            }
    }

    override fun getCompositionObservable(id: Long): Observable<Composition> {
        return settingsPreferences.displayFileNameObservable
            .switchMap { useFileName -> compositionsDao.getCompositionObservable(id, useFileName) }
    }

    override fun getFullCompositionObservable(id: Long): Observable<FullComposition> {
        return compositionsDao.getFullCompositionObservable(id)
    }

    override fun getLyricsObservable(id: Long): Observable<String> {
        return compositionsDao.getLyricsObservable(id)
    }

    override fun writeErrorAboutComposition(
        corruptionType: CorruptionType?,
        composition: Composition
    ): Completable {
        return Completable.fromAction {
            compositionsDao.writeErrorAboutComposition(composition, corruptionType)
        }.subscribeOn(ioScheduler)
    }

    override fun deleteComposition(composition: Composition): Single<DeletedComposition> {
        return Single.fromCallable {
            val id = composition.id
            var deletedComposition = compositionsDao.selectDeletedComposition(
                id,
                settingsPreferences.isDisplayFileNameEnabled
            )
            deletedComposition = storageFilesDataSource.deleteCompositionFile(deletedComposition)
            compositionsDao.delete(id)
            return@fromCallable deletedComposition
        }.subscribeOn(ioScheduler)
    }

    override fun deleteCompositions(compositions: List<CompositionModel>): Single<List<DeletedComposition>> {
        return Single.fromCallable {
            val ids = ListUtils.mapToLongArray(compositions, CompositionModel::id)
            var deletedCompositions = compositionsDao.selectDeletedComposition(
                ids,
                settingsPreferences.isDisplayFileNameEnabled
            )
            deletedCompositions = storageFilesDataSource.deleteCompositionFiles(
                deletedCompositions,
                compositions
            )
            compositionsDao.deleteAll(ids)
            return@fromCallable deletedCompositions
        }.subscribeOn(ioScheduler)
    }

    override fun getMissingCompositionsCountObservable(): Observable<Int> {
        return compositionsDao.getMissingCompositionsCountObservable()
            .retryWithDelay(10, 5, TimeUnit.SECONDS)
    }

    override fun getMissingAudioFilesObservable(): Observable<List<AudioFileInfo>> {
        return compositionsDao.getMissingAudioFilesObservable()
    }

    override fun deleteMissingCompositions(): Single<List<DeletedComposition>> {
        return compositionsDao.getMissingAudioFilesObservable().firstOrError()
            .map { missingAudioFiles ->
                compositionsDao.deleteMissingCompositions()
                return@map missingAudioFiles.map { audioFile ->
                    DeletedComposition(
                        audioFile.fileName,
                        audioFile.parentPath,
                        null,
                        audioFile.fileName // expected no-use in this case
                    )
                }
            }.subscribeOn(ioScheduler)
    }

    override fun getCompositionKeys(lookup: CompositionLookup): Single<List<FileKey>> {
        return Single.fromCallable { compositionsDao.getCompositionKeys(lookup) }
            .subscribeOn(ioScheduler)
    }

    override fun getFoldersInFolder(
        folderId: Long?,
        searchQuery: String?
    ): Observable<List<FileSource>> {
        return settingsPreferences.folderOrderObservable
            .switchMap { order ->
                settingsPreferences.displayFileNameObservable
                    .switchMap { useFileName ->
                        foldersDao.getFilesObservable(folderId, order, useFileName, searchQuery)
                    }
            }
    }

    override fun getFolderObservable(folderId: Long): Observable<FolderInfo> {
        return foldersDao.getFolderObservable(folderId)
    }

    override fun getVolumes(): Observable<List<Volume>> {
        return foldersDao.getVolumes()
    }

    override fun getAllCompositionsInFolder(folderId: Long?): Single<List<Composition>> {
        return Single.fromCallable { selectAllCompositionsInFolder(folderId) }
            .subscribeOn(ioScheduler)
    }

    override fun getAllCompositionsInFolders(fileSources: Iterable<FileSource>): Single<List<Composition>> {
        return foldersDao.extractAllCompositionsFromFiles(
            fileSources,
            settingsPreferences.folderOrder,
            settingsPreferences.isDisplayFileNameEnabled
        ).subscribeOn(ioScheduler)
    }

    override fun deleteFolder(folder: FolderFileSource): Single<List<DeletedComposition>> {
        return Single.fromCallable {
            val compositions = compositionsDao.getAllCompositionsInFolder(
                folder.id,
                settingsPreferences.isDisplayFileNameEnabled
            )
            val ids = ListUtils.mapToLongArray(compositions, Composition::id)
            var deletedCompositions = compositionsDao.selectDeletedComposition(
                ids,
                settingsPreferences.isDisplayFileNameEnabled
            )
            deletedCompositions = storageFilesDataSource.deleteCompositionFiles(
                deletedCompositions,
                folder
            )
            foldersDao.deleteFolder(folder.id, ids)
            return@fromCallable deletedCompositions
        }.subscribeOn(ioScheduler)
    }

    override fun deleteFolders(folders: List<FileSource>): Single<List<DeletedComposition>> {
        return foldersDao.extractAllCompositionsFromFiles(folders)
            .map { idList ->
                val ids = idList.toTypedArray()
                var deletedCompositions = compositionsDao.selectDeletedComposition(
                    ids,
                    settingsPreferences.isDisplayFileNameEnabled
                )
                deletedCompositions = storageFilesDataSource.deleteCompositionFiles(
                    deletedCompositions,
                    folders
                )
                foldersDao.deleteFolders(extractFolderIds(folders), ids)
                return@map deletedCompositions
            }.subscribeOn(ioScheduler)
    }

    override fun getAllParentFolders(folderId: Long?): Single<List<Long>> {
        return Single.fromCallable { foldersDao.getAllParentFoldersId(folderId) }
            .subscribeOn(ioScheduler)
    }

    override fun getAllParentFoldersForComposition(compositionId: Long): Single<List<Long>> {
        return Single.fromCallable{
            val folderId = compositionsDao.getFolderId(compositionId)
            foldersDao.getAllParentFoldersId(folderId)
        }.subscribeOn(ioScheduler)
    }

    override fun getArtistsObservable(searchText: String?): Observable<List<Artist>> {
        return settingsPreferences.artistsOrderObservable
            .switchMap { order -> artistsDao.getAllObservable(order, searchText) }
    }

    override fun getAllCompositionIdsByArtists(artistId: Long): Single<List<Long>> {
        return artistsDao.getAllCompositionIdsByArtist(artistId)
            .subscribeOn(ioScheduler)
    }

    override fun getAllCompositionIdsByArtists(artists: Iterable<Artist>): Single<List<Long>> {
        return Observable.fromIterable(artists)
            .flatMapSingle { artist -> artistsDao.getAllCompositionIdsByArtist(artist.id) }
            .collectIntoList(ArrayList<Long>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getAllCompositionsByArtists(artists: Iterable<Artist>): Single<List<Composition>> {
        return Observable.fromIterable(artists)
            .map { artist ->
                artistsDao.getAllCompositionsByArtist(
                    artist.id,
                    settingsPreferences.isDisplayFileNameEnabled
                )
            }
            .collectIntoList(ArrayList<Composition>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getAllCompositionsByArtistIds(artists: Iterable<Long>): Single<List<Composition>> {
        return Observable.fromIterable(artists)
            .map { artisId ->
                artistsDao.getAllCompositionsByArtist(
                    artisId,
                    settingsPreferences.isDisplayFileNameEnabled
                )
            }
            .collectIntoList(ArrayList<Composition>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getCompositionsByArtist(artistId: Long): Observable<List<Composition>> {
        return settingsPreferences.displayFileNameObservable
            .switchMap { useFileName ->
                artistsDao.getCompositionsByArtistObservable(artistId, useFileName)
            }
    }

    override fun getArtistObservable(artistId: Long): Observable<Artist> {
        return artistsDao.getArtistObservable(artistId)
    }

    override fun getAllAlbumsForArtist(artistId: Long): Observable<List<Album>> {
        return albumsDao.getAllAlbumsForArtistObservable(artistId)
    }

    override fun getAuthorNames(): Single<Array<String>> {
        return Single.fromCallable { artistsDao.getAuthorNames() }
            .subscribeOn(ioScheduler)
    }

    override fun getAlbumsObservable(searchText: String?): Observable<List<Album>> {
        return settingsPreferences.albumsOrderObservable
            .switchMap { order -> albumsDao.getAllObservable(order, searchText) }
    }

    override fun getAlbumItemsObservable(albumId: Long): Observable<List<AlbumComposition>> {
        return settingsPreferences.displayFileNameObservable
            .switchMap { useFileName ->
                albumsDao.getCompositionsInAlbumObservable(albumId, useFileName)
            }
    }

    override fun getCompositionIdsInAlbum(albumId: Long): Single<List<Long>> {
        return albumsDao.getCompositionIdsInAlbum(albumId)
            .subscribeOn(ioScheduler)
    }

    override fun getCompositionIdsInAlbums(albums: Iterable<Album>): Single<List<Long>> {
        return Observable.fromIterable(albums)
            .flatMapSingle { album -> albumsDao.getCompositionIdsInAlbum(album.id) }
            .collectIntoList(ArrayList<Long>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getCompositionsInAlbums(albums: Iterable<Album>): Single<List<Composition>> {
        return Observable.fromIterable(albums)
            .map { album ->
                albumsDao.getCompositionsInAlbum(
                    album.id,
                    settingsPreferences.isDisplayFileNameEnabled
                )
            }
            .collectIntoList(ArrayList<Composition>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getCompositionsByAlbumIds(albumIds: Iterable<Long>): Single<List<Composition>> {
        return Observable.fromIterable(albumIds)
            .map { albumId ->
                albumsDao.getCompositionsInAlbum(
                    albumId,
                    settingsPreferences.isDisplayFileNameEnabled
                )
            }
            .collectIntoList(ArrayList<Composition>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getAlbumObservable(albumId: Long): Observable<Album> {
        return albumsDao.getAlbumObservable(albumId)
    }

    override fun getAlbumNames(): Single<Array<String>> {
        return Single.fromCallable { albumsDao.getAlbumNames() }
            .subscribeOn(ioScheduler)
    }

    override fun getGenresObservable(searchText: String?): Observable<List<Genre>> {
        return settingsPreferences.genresOrderObservable
            .switchMap { order -> genresDao.getAllObservable(order, searchText) }
    }

    override fun getGenreItemsObservable(genreId: Long): Observable<List<Composition>> {
        return settingsPreferences.displayFileNameObservable
            .switchMap { useFileName ->
                genresDao.getCompositionsInGenreObservable(genreId, useFileName)
            }
    }

    override fun getCompositionIdsInGenres(genres: Iterable<Genre>): Single<List<Long>> {
        return Observable.fromIterable(genres)
            .flatMapSingle { playList -> genresDao.getAllCompositionIdsByGenre(playList.id) }
            .collectIntoList(ArrayList<Long>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getCompositionsInGenres(genres: Iterable<Genre>): Single<List<Composition>> {
        return Observable.fromIterable(genres)
            .map { genre ->
                genresDao.getCompositionsInGenre(
                    genre.id,
                    settingsPreferences.isDisplayFileNameEnabled
                )
            }
            .collectIntoList(ArrayList<Composition>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getCompositionsInGenresIds(genresIds: Iterable<Long>): Single<List<Composition>> {
        return Observable.fromIterable(genresIds)
            .map { genreId ->
                genresDao.getCompositionsInGenre(
                    genreId,
                    settingsPreferences.isDisplayFileNameEnabled
                )
            }
            .collectIntoList(ArrayList<Composition>::addAll)
            .subscribeOn(ioScheduler)
    }

    override fun getAllCompositionsByGenre(genreId: Long): Single<List<Long>> {
        return genresDao.getAllCompositionIdsByGenre(genreId)
            .subscribeOn(ioScheduler)
    }

    override fun getGenreNames(forCompositionId: Long): Single<Array<String>> {
        return Single.fromCallable { genresDao.getGenreNames(forCompositionId) }
            .subscribeOn(ioScheduler)
    }

    override fun getGenreObservable(genreId: Long): Observable<Genre> {
        return genresDao.getGenreObservable(genreId)
    }

    override fun addFolderToIgnore(dir: AbstractDirectory): Single<Pair<IgnoredFolder, List<FileKey>>> {
        return Single.fromCallable {
            val folderPath = foldersDao.getFullFolderPath(dir.getFolderId())
            val compositions = compositionsDao.getCompositionsInFolder(dir.getFolderId())
            val ignoredFolder = ignoredFoldersDao.insertIgnoredFolder(folderPath)
            storageScannerRepository.rescanStorage()
            return@fromCallable Pair(ignoredFolder, compositions)
        }.subscribeOn(ioScheduler)
    }

    override fun addFolderToIgnore(folder: IgnoredFolder): Single<List<FileKey>> {
        return Single.fromCallable {
            val compositions = compositionsDao.getCompositionsInFolder(folder.path)
            ignoredFoldersDao.insert(folder.path, folder.addTime)
            storageScannerRepository.rescanStorage()
            return@fromCallable compositions
        }.subscribeOn(ioScheduler)
    }

    override fun getIgnoredFoldersObservable(): Observable<List<IgnoredFolder>> {
        return ignoredFoldersDao.getIgnoredFoldersObservable()
    }

    override fun deleteIgnoredFolder(path: String): Single<List<FileKey>> {
        return Single.fromCallable { ignoredFoldersDao.deleteIgnoredFolder(path) }
            .flatMap { deletedRows ->
                if (deletedRows <= 0) {
                    return@flatMap Single.just(emptyList())
                }
                return@flatMap storageScannerRepository.runRescanStorage()
                    .andThen(Single.defer {
                        Single.just(compositionsDao.getCompositionsInFolder(path)) }
                    )

            }.subscribeOn(ioScheduler)
    }

    private fun extractFolderIds(sources: List<FileSource>): List<Long> {
        val result = LinkedList<Long>()
        for (source in sources) {
            if (source is FolderFileSource) {
                result.add(source.id)
            }
        }
        return result
    }

    private fun selectAllCompositionsInFolder(folderId: Long?): List<Composition> {
        return foldersDao.getAllCompositionsInFolder(
            folderId,
            settingsPreferences.folderOrder,
            settingsPreferences.isDisplayFileNameEnabled
        )
    }
}
