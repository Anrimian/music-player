package com.github.anrimian.musicplayer.domain.models.utils

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition

fun FullComposition.isFileExists() = storageId != null