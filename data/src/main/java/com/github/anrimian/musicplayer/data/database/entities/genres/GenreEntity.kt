package com.github.anrimian.musicplayer.data.database.entities.genres

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "genres",
    indices = [ Index(value = [ "name" ], unique = true) ]
)
class GenreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String
)