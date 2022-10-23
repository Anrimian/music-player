package com.github.anrimian.filesync.models.repo

enum class DisableReason {
    LOGOUT,
    REMOTE_VERSION_IS_TOO_HIGH,
    SPACE_IS_FULL,
    WRONG_FILE_PATH,
}