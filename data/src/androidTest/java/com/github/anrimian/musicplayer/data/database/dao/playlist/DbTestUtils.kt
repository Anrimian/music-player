package com.github.anrimian.musicplayer.data.database.dao.playlist

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

object DbTestUtils {

    fun insert(
        compositionsDao: CompositionsDao,
        artistId: Long?,
        albumId: Long?,
        title: String?,
        folderId: Long? = null
    ) = compositionsDao.insert(
        artistId = artistId,
        albumId = albumId,
        folderId = folderId,
        title = title,
        trackNumber = null,
        discNumber = null,
        comment = null,
        lyrics = null,
        fileName = "test file path",
        duration = 100L,
        size = 100L,
        storageId = null,
        addedTime = System.currentTimeMillis(),
        modifiedTime = System.currentTimeMillis(),
        storageModifyTime = 0,
        pathModifyTime = 0,
        lastScanTime = 0,
        coverModifyTime = 0,
        localFileStatus = LocalFileStatus.AVAILABLE,
        corruptionType = null,
        initialSource = InitialSource.LOCAL
    )
}
