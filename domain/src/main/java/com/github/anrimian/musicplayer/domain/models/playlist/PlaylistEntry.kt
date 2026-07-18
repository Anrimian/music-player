package com.github.anrimian.musicplayer.domain.models.playlist

import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

data class PlaylistEntry(
    val entryId: Long,
    override val id: Long,
    override val title: String,
    override val artist: String?,
    override val album: String?,
    override val duration: Long,
    override val size: Long,
    override val comment: String?,
    override val storageId: Long?,
    override val addedTime: Long,
    override val modifiedTime: Long,
    override val coverModifyTime: Long,
    override val fileStatus: LocalFileStatus,
    override val corruptionType: CorruptionType?,
    override val isFileExists: Boolean,
    override val initialSource: InitialSource
): CompositionModel