package com.github.anrimian.fsync.models

interface SyncEnvCondition {
    fun canBeSkipped(): Boolean
}