package com.github.anrimian.musicplayer.domain.models.equalizer

class EqualizerConfig(
    val lowestBandRange: Short,
    val highestBandRange: Short,
    val bands: List<Band>,
    val presets: List<Preset>
)
