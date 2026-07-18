package com.github.anrimian.musicplayer.infrastructure.service.wearable

import android.content.Context
import com.github.anrimian.common.WearableConstants
import com.github.anrimian.common.WearableFields
import com.github.anrimian.data.WearableModelsHelper
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState

class WearableStateHolder(context: Context) {

    private val preferences = SharedPreferencesHelper(
        context.getSharedPreferences(WEARABLE_STATE, Context.MODE_PRIVATE)
    )

    private var isPlaying: Boolean = false
    private var queueItemItem: Long? = null
    private var currentComposition: WearableComposition? = null
    private var currentVolume: VolumeState? = null
    private var trackPosition: Long? = null
    private var playbackSpeed: Float? = null
    private var isRandom: Boolean? = null
    private var repeatMode: Int? = null

    fun setProtocolVersion(version: Int) {
        preferences.putInt(PROTOCOL_VERSION, version)
    }

    fun getProtocolVersion(): Int = preferences.getInt(PROTOCOL_VERSION, WearableConstants.WEARABLE_PROTOCOL_VERSION)

    fun applyPlayingState(isPlaying: Boolean): Long? {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying
            return updateAndGetStateUpdateTime()
        }
        return null
    }

    fun isPlaying() = isPlaying

    fun applyCurrentQueueItemId(id: Long?): Long? {
        val itemId = id ?: 0
        if (this.queueItemItem != itemId) {
            this.queueItemItem = itemId
            preferences.putLong(WearableFields.QUEUE_ITEM_ID, itemId)
            return updateAndGetStateUpdateTime()
        }
        return null
    }

    fun getCurrentQueueItemId(): Long? {
        if (queueItemItem == null) {
            queueItemItem = preferences.getLong(WearableFields.QUEUE_ITEM_ID)
        }
        if (queueItemItem == 0L) {
            return null
        }
        return queueItemItem
    }

    fun applyCurrentComposition(source: WearableComposition?): Long? {
        if (!WearableModelsHelper.areContentsTheSame(source, getCurrentComposition())) {
            this.currentComposition = source
            WearableModelsHelper.writeComposition(source, preferences)
            return updateAndGetStateUpdateTime()
        }
        return null
    }

    fun getCurrentComposition(): WearableComposition? {
        if (currentComposition == null) {
            currentComposition = WearableModelsHelper.readComposition(preferences)
        }
        return currentComposition
    }

    fun applyVolumeState(volumeState: VolumeState): Long? {
        if (volumeState != getVolume()) {
            currentVolume = volumeState
            preferences.putLong(WearableFields.VOLUME, volumeState.toLong())
            return updateAndGetStateUpdateTime()
        }
        return null
    }

    fun getVolume(): VolumeState {
        if (currentVolume == null) {
            currentVolume = VolumeState.from(preferences.getLong(WearableFields.VOLUME))
        }
        return currentVolume!!
    }

    fun getLastUpdateTime(): Long {
        return preferences.getLong(WearableFields.UPDATE_TIME)
    }

    fun setTrackPosition(position: Long): Boolean {
        if (position != getTrackPosition()) {
            this.trackPosition = position
            preferences.putLong(TRACK_POSITION, position)
            return true
        }
        return false
    }

    fun getTrackPosition(): Long {
        if (trackPosition == null) {
            trackPosition = preferences.getLong(TRACK_POSITION)
        }
        return trackPosition!!
    }

    fun setPlaybackSpeed(playbackSpeed: Float): Long? {
        if (playbackSpeed != getPlaybackSpeed()) {
            this.playbackSpeed = playbackSpeed
            preferences.putFloat(PLAYBACK_SPEED, playbackSpeed)
            return updateAndGetStateUpdateTime()
        }
        return null
    }

    fun getPlaybackSpeed(): Float {
        if (playbackSpeed == null) {
            playbackSpeed = preferences.getFloat(PLAYBACK_SPEED)
        }
        return playbackSpeed!!
    }

    fun setRandomMode(isRandom: Boolean): Long? {
        if (isRandom != getRandomMode()) {
            this.isRandom = isRandom
            preferences.putBoolean(RANDOM_MODE, isRandom)
            return updateAndGetStateUpdateTime()
        }
        return null
    }

    fun getRandomMode(): Boolean {
        if (isRandom == null) {
            isRandom = preferences.getBoolean(RANDOM_MODE)
        }
        return isRandom!!
    }

    fun setRepeatMode(repeatMode: Int): Long? {
        if (repeatMode != getRepeatMode()) {
            this.repeatMode = repeatMode
            preferences.putInt(REPEAT_MODE, repeatMode)
            return updateAndGetStateUpdateTime()
        }
        return null
    }

    fun getRepeatMode(): Int {
        if (repeatMode == null) {
            repeatMode = preferences.getInt(REPEAT_MODE)
        }
        return repeatMode!!
    }

    private fun updateAndGetStateUpdateTime(): Long {
        val stateUpdateTime = System.currentTimeMillis()
        preferences.putLong(WearableFields.UPDATE_TIME, stateUpdateTime)
        return stateUpdateTime
    }

    private companion object {
        const val WEARABLE_STATE = "wearable_state"
        const val PROTOCOL_VERSION = "protocol_version"
        const val TRACK_POSITION = "track_position"
        const val PLAYBACK_SPEED = "playback_speed"
        const val RANDOM_MODE = "random_mode"
        const val REPEAT_MODE = "repeat_mode"
    }

}