package com.github.anrimian.musicplayer.wear.data

import android.content.Context
import android.util.Log
import com.github.anrimian.common.WearableFields
import com.github.anrimian.data.WearableModelsHelper
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.wear.Constants
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class WearStateRepository(context: Context) {

    private val preferences = SharedPreferencesHelper(
        context.getSharedPreferences(WEARABLE_STATE, Context.MODE_PRIVATE)
    )

    private val isPlayingSubject = BehaviorSubject.createDefault(false)
    private val currentCompositionSubject = BehaviorSubject.create<Opt<WearableComposition>>()
    private val hostVersionSubject = BehaviorSubject.create<Int>()
    private val volumeStateSubject = BehaviorSubject.create<VolumeState>()

    fun setLastUpdateTime(time: Long) {
        preferences.putLong(WearableFields.UPDATE_TIME, time)
    }

    fun getLastUpdateTime(): Long {
        return preferences.getLong(WearableFields.UPDATE_TIME, WearableFields.NO_STATE_TIME)
    }

    fun setIsPlaying(isPlying: Boolean) {
        isPlayingSubject.onNext(isPlying)
    }

    fun isPlaying() = isPlayingSubject.value!!

    fun getIsPlayingObservable(): Observable<Boolean> = isPlayingSubject

    fun setCurrentComposition(composition: WearableComposition?) {
        WearableModelsHelper.writeComposition(composition, preferences)
        currentCompositionSubject.onNext(Opt(composition))
    }

    fun getCurrentComposition(): WearableComposition? {
        return WearableModelsHelper.readComposition(preferences)
    }

    fun getCurrentCompositionObservable(): Observable<Opt<WearableComposition>> {
        return RxUtils.withDefaultValue(currentCompositionSubject) { Opt(getCurrentComposition()) }
    }


    fun setPlayQueueItemId(itemId: Long?) {
        preferences.putLong(WearableFields.QUEUE_ITEM_ID, itemId ?: 0)
    }

    fun setHostProtocolVersion(version: Int) {
        if (version != getHostProtocolVersion()) {
            preferences.putInt(HOST_PROTOCOL_VERSION, version)
            hostVersionSubject.onNext(version)
        }
    }

    fun getHostProtocolVersion(): Int = preferences.getInt(HOST_PROTOCOL_VERSION, Constants.UNKNOWN_HOST_PROTOCOL_VERSION)

    fun getHostProtocolVersionObservable(): Observable<Int> {
        return RxUtils.withDefaultValue(hostVersionSubject, ::getHostProtocolVersion)
    }

    fun setCurrentTrackPosition(position: Long) {
        if (position != getCurrentTrackPosition()) {
            preferences.putLong(TRACK_POSITION, position)
        }
    }

    fun getCurrentTrackPosition(): Long {
        return preferences.getLong(TRACK_POSITION)
    }

    fun setCurrentVolume(volume: Int) {
        val volumeState = getVolumeState()
        if (volumeState.getVolume() != volume) {
            val newState = VolumeState.from(volume, volumeState.getMaxVolume())
            preferences.putLong(VOLUME_STATE, newState.toLong())
            volumeStateSubject.onNext(newState)
        }
    }

    fun setVolumeState(newState: VolumeState) {
        val currentState = getVolumeState()
        if (newState == currentState) {
            return
        }
        preferences.putLong(VOLUME_STATE, newState.toLong())
        Log.d("KEK", "setVolumeState: $newState")
        volumeStateSubject.onNext(newState)
    }

    fun getVolumeState(): VolumeState {
        val value = volumeStateSubject.value
        if (value != null) {
            return value
        }
        val volumeState = preferences.getLong(VOLUME_STATE, Constants.NO_STATE)
        return VolumeState.from(volumeState)
    }

    fun getVolumeStateObservable(): Observable<VolumeState> {
        return RxUtils.withDefaultValue(volumeStateSubject, ::getVolumeState)
    }

    private companion object {
        const val WEARABLE_STATE = "wearable_state"
        const val HOST_PROTOCOL_VERSION = "host_protocol_version"
        const val TRACK_POSITION = "track_position"
        const val VOLUME_STATE = "volume_state"
    }

}