package com.github.anrimian.fsync.models.storage

enum class DisableReason {
    MANUAL,
    LOGOUT,
    REMOTE_VERSION_IS_TOO_HIGH,
    REMOTE_SPACE_IS_FULL,
    LOCAL_SPACE_IS_FULL,
}