package com.github.anrimian.filesync.models.repo

class RemoteRepoInfo(val id: Long, val repoType: RemoteRepoType, val accountInfo: RepoAccountInfo) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteRepoInfo

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "RemoteRepositoryInfo(id=$id, repoType=$repoType)"
    }


}