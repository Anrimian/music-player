package com.github.anrimian.musicplayer.data.database.entities.composition

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

@Entity(
    tableName = "compositions",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "artistId" ]
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "albumId" ]
        ),
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "folderId" ]
        )
    ],
    indices = [ Index("artistId"), Index("albumId"), Index("folderId") ]
)
class CompositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val artistId: Long?,
    val albumId: Long?,
    val folderId: Long?,
    val title: String?,
    val trackNumber: Long?,
    val discNumber: Long?,
    val comment: String?,
    val lyrics: String?,
    val fileName: String,
    val duration: Long,
    val size: Long,
    val storageId: Long?,
    val addedTime: Long,
    val modifiedTime: Long,
    val storageModifyTime: Long,
    val pathModifyTime: Long?,
    val lastScanTime: Long,
    val missingTime: Long,
    val coverModifyTime: Long,
    val localFileStatus: LocalFileStatus,
    val corruptionType: CorruptionType?,
    val initialSource: InitialSource
)
