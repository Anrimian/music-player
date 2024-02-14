package com.github.anrimian.musicplayer.data.database.entities.genres

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity

@Entity(
    tableName = "genre_entries",
    primaryKeys = [ "genreId", "compositionId" ],
    foreignKeys = [
        ForeignKey(
            entity = CompositionEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "compositionId" ],
            onDelete = ForeignKey.CASCADE
        ), ForeignKey(
            entity = GenreEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "genreId" ],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [ Index(value = [ "compositionId" ]) ]
)
class GenreEntryEntity(
    val genreId: Long,
    val compositionId: Long,
    val position: Int
)