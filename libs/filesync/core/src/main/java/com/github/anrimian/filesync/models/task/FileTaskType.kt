package com.github.anrimian.filesync.models.task

enum class FileTaskType {
    LOCAL_DELETE,
    REMOTE_DELETE,
    DOWNLOAD,
    UPLOAD,
    LOCAL_MOVE,
    REMOTE_MOVE
}