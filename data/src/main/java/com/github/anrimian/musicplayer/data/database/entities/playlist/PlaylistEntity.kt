package com.github.anrimian.musicplayer.data.database.entities.playlist

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_lists",
    indices = [ Index(value = [ "name" ], unique = true) ]
)
class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val storageId: Long?,
    val name: String,
    val addedTime: Long,
    val modifiedTime: Long
)
