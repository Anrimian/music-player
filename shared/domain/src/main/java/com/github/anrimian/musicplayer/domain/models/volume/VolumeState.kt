package com.github.anrimian.musicplayer.domain.models.volume

@JvmInline
value class VolumeState private constructor(private val value: Long) {
    fun getVolume(): Int {
        return (value shr 32).toInt()
    }

    fun getMaxVolume(): Int {
        return value.toInt()
    }

    fun toLong() = value

    override fun toString(): String {
        return "VolumeState(volume=${getVolume()}, max=${getMaxVolume()})"
    }

    companion object {
        fun from(volume: Int, maxVolume: Int): VolumeState {
            return VolumeState(volume.toLong() shl 32 or (maxVolume.toLong() and 0xffffffffL))
        }
        fun from(rawValue: Long) = VolumeState(rawValue)
    }
}