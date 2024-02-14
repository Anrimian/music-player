package com.github.anrimian.musicplayer.data.database.entities.folder

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ignored_folders")
class IgnoredFolderEntity(
    @PrimaryKey
    val relativePath: String,
    val addDate: Date
)