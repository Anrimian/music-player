package com.github.anrimian.musicplayer.data.database.dao.ignoredfolders

import androidx.room.Dao
import androidx.room.Query
import com.github.anrimian.musicplayer.domain.models.exceptions.FolderAlreadyIgnoredException
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import io.reactivex.rxjava3.core.Observable
import java.util.Date

@Dao
interface IgnoredFoldersDao {

    fun insertIgnoredFolder(path: String): IgnoredFolder {
        val addDate = Date()
        val id = insert(path, Date())
        if (id == -1L) {
            throw FolderAlreadyIgnoredException()
        }
        return IgnoredFolder(path, addDate)
    }

    @Query("""
        INSERT OR IGNORE INTO ignored_folders (relativePath, addDate) 
        VALUES (:relativePath, :addDate)
    """)
    fun insert(relativePath: String, addDate: Date): Long

    @Query("SELECT relativePath FROM ignored_folders")
    fun getIgnoredFolders(): Array<String>

    @Query("SELECT relativePath, addDate FROM ignored_folders ORDER BY addDate")
    fun getIgnoredFoldersObservable(): Observable<List<IgnoredFolder>>

    @Query("DELETE FROM ignored_folders WHERE relativePath = :path")
    fun deleteIgnoredFolder(path: String): Int

}