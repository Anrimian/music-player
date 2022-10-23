package com.github.anrimian.filesync.models.repo

class RepoSpaceUsage(val used: Long, val total: Long)
fun unknownSpaceUsage() = RepoSpaceUsage(-1L, -1L)
fun unlimitedSpaceUsage() = RepoSpaceUsage(-1L, Long.MIN_VALUE)