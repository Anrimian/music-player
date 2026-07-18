package com.github.anrimian.fsync.models.run

enum class ScheduledTaskRunResult {
    COMPLETED,
    CONTINUATION_REQUIRED,
    RETRY
}
