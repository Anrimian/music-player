package com.github.anrimian.musicplayer.data.database.entities.play_queue

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_positions",
    foreignKeys = [
        ForeignKey(
            entity = PlayQueueEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "queueItemId" ],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class TrackPositionEntity(
    @PrimaryKey
    val queueItemId: Long,
    val trackPosition: Long,
    val writeTime: Long
)