package com.github.anrimian.musicplayer.data.database.entities.playlist

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity

@Entity(
    tableName = "play_lists_entries",
    foreignKeys = [
        ForeignKey(
            entity = CompositionEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "audioId" ],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "playListId" ],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("audioId"),
        Index("playListId")
    ]
)
class PlaylistEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long,
    val storageItemId: Long?,
    val audioId: Long,
    val playListId: Long,
    val orderPosition: Int
)
