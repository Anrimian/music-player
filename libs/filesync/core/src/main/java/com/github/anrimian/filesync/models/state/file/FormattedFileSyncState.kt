package com.github.anrimian.filesync.models.state.file

class FormattedFileSyncState {
    lateinit var fileSyncState: FileSyncState
    var isFileRemote: Boolean = true

    fun set(fileSyncState: FileSyncState, isFileRemote: Boolean): FormattedFileSyncState {
        this.fileSyncState = fileSyncState
        this.isFileRemote = isFileRemote
        return this
    }
}