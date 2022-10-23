package com.github.anrimian.filesync.models.repo

data class DisableState(
    val disableReason: DisableReason,
    val message: String?
)