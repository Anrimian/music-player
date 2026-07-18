package com.github.anrimian.musicplayer.data.database.entities.folder

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "volumes",
    indices = [ Index("path", unique = true) ]
)
class VolumeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val storageKey: String,
    val path: String,
    val isPrimary: Boolean
)