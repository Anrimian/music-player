package com.github.anrimian.musicplayer.domain.models.folders

import java.util.Date

class IgnoredFolder(val relativePath: String, val addDate: Date) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IgnoredFolder

        if (relativePath != other.relativePath) return false

        return true
    }

    override fun hashCode(): Int {
        return relativePath.hashCode()
    }

}