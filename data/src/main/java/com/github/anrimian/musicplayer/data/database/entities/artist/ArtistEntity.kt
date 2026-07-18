package com.github.anrimian.musicplayer.data.database.entities.artist

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    indices = [ Index(value = [ "name" ], unique = true) ]
)
class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String
)
