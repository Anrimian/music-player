package com.github.anrimian.filesync.models.task

import com.github.anrimian.filesync.models.repo.RemoteRepoInfo

class FileTaskInfo<K>(
    val task: Task<K>,
    val taskCreateTime: Long,
    val taskCompleteTime: Long?,
    val excludeReason: ExcludeReason?,
    val repoInfo: RemoteRepoInfo,
    val taskReason: Int,
    val reasonDescription: String?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileTaskInfo<*>

        if (task != other.task) return false

        return true
    }

    override fun hashCode(): Int {
        return task.hashCode()
    }
}