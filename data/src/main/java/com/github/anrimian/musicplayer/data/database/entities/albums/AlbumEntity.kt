package com.github.anrimian.musicplayer.data.database.entities.albums

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity

@Entity(
    tableName = "albums",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "artistId" ]
        )
    ],
    indices = [ Index("artistId"), Index(value = [ "artistId", "name" ], unique = true) ]
)
class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val artistId: Long?,
    val name: String
)
