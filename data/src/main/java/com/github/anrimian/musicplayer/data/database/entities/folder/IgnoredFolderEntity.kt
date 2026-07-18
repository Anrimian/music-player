package com.github.anrimian.musicplayer.data.database.entities.folder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ignored_folders")
class IgnoredFolderEntity(
    @PrimaryKey
    val path: String,
    val addTime: Long
)