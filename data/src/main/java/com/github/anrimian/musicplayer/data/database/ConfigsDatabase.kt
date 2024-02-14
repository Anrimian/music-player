package com.github.anrimian.musicplayer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.anrimian.musicplayer.data.database.converters.DateConverter
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao
import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity

@Database(
    entities = [ IgnoredFolderEntity::class ],
    version = 1
)
@TypeConverters(DateConverter::class)
abstract class ConfigsDatabase: RoomDatabase() {

    abstract fun ignoredFoldersDao(): IgnoredFoldersDao

}