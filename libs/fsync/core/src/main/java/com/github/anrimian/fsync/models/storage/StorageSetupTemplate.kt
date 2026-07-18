package com.github.anrimian.fsync.models.storage

class StorageSetupTemplate(
    val credentials: RemoteStorageCredentials,
    var remoteRootPath: String,
    var localRootPath: String,
    var accountInfo: StorageAccountInfo? = null,
    var spaceUsage: StorageSpaceUsage = unknownSpaceUsage(),
) {

    fun getStorageType() = credentials.getStorageType()

}