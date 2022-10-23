package com.github.anrimian.filesync.models.repo

class RemoteRepoFullInfo(
    val remoteRepoInfo: RemoteRepoInfo,
    val spaceUsage: RepoSpaceUsage,
    val disableState: DisableState?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteRepoFullInfo

        if (remoteRepoInfo != other.remoteRepoInfo) return false

        return true
    }

    override fun hashCode(): Int {
        return remoteRepoInfo.hashCode()
    }
}