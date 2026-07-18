package com.github.anrimian.musicplayer.data.models.exceptions

import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel

class DuplicatePlaylistEntriesException(
    val duplicates: Collection<CompositionModel>,
    val hasNonDuplicates: Boolean
) : RuntimeException()