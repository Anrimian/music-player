package com.github.anrimian.musicplayer.data.models.exceptions

import com.github.anrimian.musicplayer.domain.models.composition.Composition

class DuplicatePlaylistEntriesException(
    val duplicates: MutableCollection<Composition>,
    val hasNonDuplicates: Boolean
) : RuntimeException()