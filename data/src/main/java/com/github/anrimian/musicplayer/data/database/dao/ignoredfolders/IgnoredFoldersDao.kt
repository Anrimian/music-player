package com.github.anrimian.musicplayer.data.database.dao.ignoredfolders

import androidx.room.Dao
import androidx.room.Query
import com.github.anrimian.musicplayer.domain.models.exceptions.FolderAlreadyIgnoredException
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import io.reactivex.rxjava3.core.Observable

@Dao
interface IgnoredFoldersDao {

    fun insertIgnoredFolder(path: String): IgnoredFolder {
        val addTime = System.currentTimeMillis()
        val id = insert(path, addTime)
        if (id == -1L) {
            throw FolderAlreadyIgnoredException()
        }
        return IgnoredFolder(path, addTime)
    }

    @Query(
        """
        INSERT OR IGNORE INTO ignored_folders (path, addTime) 
        VALUES (:path, :addTime)
    """
    )
    fun insert(path: String, addTime: Long): Long

    @Query("SELECT path FROM ignored_folders")
    fun getIgnoredFolders(): Array<String>

    @Query("SELECT path, addTime FROM ignored_folders ORDER BY addTime")
    fun getIgnoredFoldersObservable(): Observable<List<IgnoredFolder>>

    @Query("DELETE FROM ignored_folders WHERE path = :path")
    fun deleteIgnoredFolder(path: String): Int

}