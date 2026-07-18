package com.github.anrimian.musicplayer.data.database.entities.play_queue

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity

@Entity(
    tableName = "play_queue",
    foreignKeys = [
        ForeignKey(
            entity = CompositionEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "audioId" ],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("audioId"),
        Index(value = [ "position" ], unique = true),
        Index(value = [ "shuffledPosition" ], unique = true)
    ]
)
class PlayQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val audioId: Long,
    val position: Int,
    val shuffledPosition: Int
)
