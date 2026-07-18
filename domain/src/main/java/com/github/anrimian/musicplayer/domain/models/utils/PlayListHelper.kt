package com.github.anrimian.musicplayer.domain.models.utils

import com.github.anrimian.musicplayer.domain.models.playlist.Playlist

object PlayListHelper {

    @JvmStatic
    fun areItemsTheSame(oldSource: Playlist, newSource: Playlist): Boolean {
        return oldSource.id == newSource.id
    }

    fun areSourcesTheSame(oldSource: Playlist, newSource: Playlist): Boolean {
        return !hasChanges(oldSource, newSource)
    }

    fun hasChanges(first: Playlist, second: Playlist): Boolean {
        return first.name != second.name
                || first.addedTime != second.addedTime
                || first.modifiedTime != second.modifiedTime
                || first.compositionsCount != second.compositionsCount
                || first.totalDuration != second.totalDuration
    }

}
