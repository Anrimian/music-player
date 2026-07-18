package com.github.anrimian.musicplayer.domain.models.composition

interface CompositionModel {
    val id: Long
    val title: String
    val artist: String?
    val album: String?
    val duration: Long
    val size: Long
    val comment: String?
    val storageId: Long?
    val addedTime: Long
    val modifiedTime: Long
    val coverModifyTime: Long
    val fileStatus: LocalFileStatus
    val corruptionType: CorruptionType?
    val isFileExists: Boolean
    val initialSource: InitialSource
}