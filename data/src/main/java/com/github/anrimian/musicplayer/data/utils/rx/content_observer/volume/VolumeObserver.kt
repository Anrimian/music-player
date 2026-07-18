package com.github.anrimian.musicplayer.data.utils.rx.content_observer.volume

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.github.anrimian.musicplayer.data.utils.rx.receivers.RxReceivers
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

object VolumeObserver {
    /**
     * Emits volume in absolute values. Has no start event
     */
    fun getVolumeObservable(context: Context, audioManager: AudioManager): Observable<Int> {
        return RxReceivers.from("android.media.VOLUME_CHANGED_ACTION", context)
            .flatMapSingle { safeGetStreamVolumeSingle(audioManager) }
            .distinctUntilChanged()
    }

    /**
     * Emits volume in state model. Has start event
     */
    fun getVolumeStateObservable(
        context: Context,
        audioManager: AudioManager,
    ): Observable<VolumeState> {
        val volumeChangesObservable = RxReceivers.from("android.media.VOLUME_CHANGED_ACTION", context)
            .map { Constants.TRIGGER }
        val deviceChangeObservable = getAudioDeviceObservable(audioManager)

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return Observable.merge(volumeChangesObservable, deviceChangeObservable)
            .startWithItem(Constants.TRIGGER)
            .flatMapSingle { safeGetStreamVolumeSingle(audioManager) }
            .map { volume -> VolumeState.from(volume, maxVolume) }
            .distinctUntilChanged()
    }

    fun safeGetStreamVolume(audioManager: AudioManager): Int {
        return try {
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        } catch (_: Exception) {
            0
        }
    }

    private fun safeGetStreamVolumeSingle(audioManager: AudioManager): Single<Int> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
            } catch (_: Exception) {}
        }
    }

    private fun getAudioDeviceObservable(audioManager: AudioManager): Observable<Any> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Observable.never()
        }
        return Observable.create { emitter ->
            val callback = object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                    emitter.onNext(Constants.TRIGGER)
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
                    emitter.onNext(Constants.TRIGGER)
                }
            }
            val audioDeviceCallback = callback
            audioManager.registerAudioDeviceCallback(callback, null)

            emitter.setCancellable {
                audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
            }
        }
    }

}
