package com.github.anrimian.musicplayer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.anrimian.musicplayer.data.database.converters.DateConverter
import com.github.anrimian.musicplayer.data.database.converters.EnumConverter
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistDao
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity
import com.github.anrimian.musicplayer.data.database.entities.folder.VolumeEntity
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntity
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntryEntity
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity
import com.github.anrimian.musicplayer.data.database.entities.play_queue.TrackPositionEntity
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlaylistEntity
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlaylistEntryEntity

@Database(
    entities = [
        PlayQueueEntity::class,
        CompositionEntity::class,
        PlaylistEntity::class,
        PlaylistEntryEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        GenreEntity::class,
        GenreEntryEntity::class,
        FolderEntity::class,
        VolumeEntity::class,
        TrackPositionEntity::class
    ],
    version = 20
)
@TypeConverters(
    DateConverter::class,
    EnumConverter::class
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun playQueueDao(): PlayQueueDao
    abstract fun compositionsDao(): CompositionsDao
    abstract fun playListDao(): PlaylistDao
    abstract fun artistsDao(): ArtistsDao
    abstract fun albumsDao(): AlbumsDao
    abstract fun genreDao(): GenreDao
    abstract fun foldersDao(): FoldersDao
}
