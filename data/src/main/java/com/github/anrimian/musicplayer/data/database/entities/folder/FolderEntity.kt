package com.github.anrimian.musicplayer.data.database.entities.folder

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "parentId" ],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VolumeEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "volumeId" ],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [ Index("parentId"), Index("volumeId") ]
)
class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val parentId: Long?,
    val volumeId: Long?, // only non null when parentId is null(root folders)
    val name: String
)
