package com.github.anrimian.fsync.models.storage

class StorageSpaceUsage(val used: Long, val total: Long)
fun unknownSpaceUsage() = StorageSpaceUsage(UNKNOWN_USED_SPACE, UNKNOWN_MAX_SPACE)
fun unlimitedSpaceUsage() = StorageSpaceUsage(UNKNOWN_USED_SPACE, Long.MIN_VALUE)

const val UNKNOWN_USED_SPACE = -1L
const val UNKNOWN_MAX_SPACE = -1L