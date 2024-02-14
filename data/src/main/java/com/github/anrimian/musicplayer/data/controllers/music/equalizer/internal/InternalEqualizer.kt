package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal

import android.media.audiofx.Equalizer
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.equalizer.Band
import com.github.anrimian.musicplayer.domain.models.equalizer.EqInitializationState
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

//two instances of eq are not allowed? - done, doesn't help
//release and nullify on detach? - done, doesn't help
//attachEqualizer - what if session id was changed? - reinit - done
//always call release to eq and android media player - done in player, always call on pause
//try to init twice?
// + 4/5 errors caused from fragment onFirstViewAttach()
//always attach session id(do not use audio session id = 0) for using correct audio session id(can't do with android media player) - skip
//calling before media player is prepared?(MediaPlayer.setOnCompletionListener)
//last resort: retry(done) + handle errors(done)
//implement error state - done on constructor
class InternalEqualizer(
    private val equalizerStateRepository: EqualizerStateRepository,
    analytics: Analytics
) : AppEqualizer {

    private val currentStateSubject = BehaviorSubject.create<EqualizerState?>()
    private val equalizerHolder = EqualizerObjectHolder(analytics::processNonFatalError)

    override fun attachEqualizer(audioSessionId: Int) {
        if (audioSessionId != 0) {
            val equalizer = equalizerHolder.initEqualizer(audioSessionId) { eq ->
                val equalizerState = equalizerStateRepository.loadEqualizerState()
                if (equalizerState != null) {
                    applyEqualizerState(eq, equalizerState)
                    currentStateSubject.onNext(equalizerState)
                } else {
                    currentStateSubject.onNext(extractEqualizerState(eq))
                }
            }
            if (equalizer != null) {
                equalizer.enabled = true
            }
        }
    }

    override fun detachEqualizer(audioSessionId: Int) {
        equalizerHolder.releaseEqualizer()
    }

    fun getEqualizerConfig(): Single<EqualizerConfig> {
        return Single.fromCallable { equalizerHolder.useEqualizer(::extractEqualizerInfo) }
    }

    fun getEqualizerStateObservable(): Observable<EqualizerState> {
        return RxUtils.withDefaultValue(currentStateSubject) {
            var equalizerState = equalizerStateRepository.loadEqualizerState()
            if (equalizerState == null) {
                equalizerState = equalizerHolder.useEqualizer(::extractEqualizerState)
            }
            equalizerState
        }
    }

    fun getEqInitializationState(): Observable<EqInitializationState> {
        return equalizerHolder.getEqInitializationState()
    }

    fun tryToReattachEqualizer() {
        equalizerHolder.tryToReattachEqualizer()
    }

    fun setBandLevel(bandNumber: Short, level: Short) {
        val equalizer = equalizerHolder.getEqualizer()
        if (equalizer != null) {
            try {
                equalizer.setBandLevel(bandNumber, level)
            } catch (e: RuntimeException) {
                val equalizerState = currentStateSubject.value
                if (equalizerState != null) {
                    currentStateSubject.onNext(equalizerState)
                }
                throw e
            }
        }
        var equalizerState = currentStateSubject.value
        if (equalizerState == null) {
            equalizerState = EqualizerState(EqualizerStateRepository.NO_PRESET, HashMap())
        }
        equalizerState.bendLevels[bandNumber] = level
        equalizerState.currentPreset = EqualizerStateRepository.NO_PRESET
        currentStateSubject.onNext(equalizerState)
    }

    fun saveBandLevel() {
        val equalizerState = currentStateSubject.value
        if (equalizerState != null) {
            equalizerStateRepository.saveEqualizerState(equalizerState)
        }
    }

    fun setPreset(preset: Preset) {
        val equalizerState = equalizerHolder.useEqualizer { eq ->
            if (preset.number <= eq.numberOfPresets) {
                eq.usePreset(preset.number)
            }
            extractEqualizerState(eq)
        }
        equalizerStateRepository.saveEqualizerState(equalizerState)
        currentStateSubject.onNext(equalizerState)
    }

    fun release() {
        equalizerHolder.releaseEqualizer()
    }

    private fun applyEqualizerState(equalizer: Equalizer, equalizerState: EqualizerState) {
        for ((key, value) in equalizerState.bendLevels) {
            EqualizerObjectHolder.setBandLevel(equalizer, key, value)
        }
    }

    private fun extractEqualizerState(equalizer: Equalizer): EqualizerState {
        val maps: MutableMap<Short, Short> = HashMap()
        for (i in 0 until equalizer.numberOfBands) {
            maps[i.toShort()] = equalizer.getBandLevel(i.toShort())
        }
        return EqualizerState(equalizer.currentPreset, maps)
    }

    private fun extractEqualizerInfo(equalizer: Equalizer): EqualizerConfig {
        val bandLevelRange = equalizer.bandLevelRange
        val lowestRange = bandLevelRange[0]
        val highestRange = bandLevelRange[1]
        val bands: MutableList<Band> = ArrayList()
        for (i in 0 until equalizer.numberOfBands) {
            bands.add(Band(i.toShort(), equalizer.getCenterFreq(i.toShort())))
        }
        val presets: MutableList<Preset> = ArrayList()
        for (i in 0 until equalizer.numberOfPresets) {
            val preset = Preset(i.toShort(), equalizer.getPresetName(i.toShort()))
            presets.add(preset)
        }
        return EqualizerConfig(lowestRange, highestRange, bands, presets)
    }

    private class EqualizerObjectHolder(
        private val onInitializationError: (Throwable) -> Unit
    ) {

        private val stateSubject = BehaviorSubject.createDefault(EqInitializationState.IDLE)

        private var equalizer: Equalizer? = null
        private var currentAudioSessionId = DEFAULT_AUDIO_SESSION_ID

        private var deferredInitFunc: ((Equalizer) -> Unit)? = null

        fun initEqualizer(audioSessionId: Int, initFunc: (Equalizer) -> Unit): Equalizer? {
            synchronized(this) {
                if (currentAudioSessionId != audioSessionId) {
                    releaseEqualizer()
                }
                if (equalizer == null) {
                    currentAudioSessionId = audioSessionId
                    equalizer = newEqualizer(EQ_PRIORITY, audioSessionId)
                    if (equalizer == null) {
                        deferredInitFunc = initFunc
                        stateSubject.onNext(EqInitializationState.INITIALIZATION_ERROR)
                    } else {
                        try {
                            initFunc(equalizer!!)
                            stateSubject.onNext(EqInitializationState.INITIALIZED)
                        } catch (e: RuntimeException) {
                            equalizer!!.release()
                            equalizer = null
                            deferredInitFunc = initFunc
                            stateSubject.onNext(EqInitializationState.INITIALIZATION_ERROR)
                            return null
                        }
                    }
                }
                return equalizer
            }
        }

        fun tryToReattachEqualizer() {
            if (equalizer == null && currentAudioSessionId != DEFAULT_AUDIO_SESSION_ID && deferredInitFunc != null && stateSubject.value == EqInitializationState.INITIALIZATION_ERROR) {
                equalizer = newEqualizer(EQ_PRIORITY, currentAudioSessionId)
                if (equalizer != null) {
                    try {
                        deferredInitFunc!!.invoke(equalizer!!)
                    } catch (e: RuntimeException) {
                        equalizer!!.release()
                        equalizer = null
                        return
                    }
                    deferredInitFunc = null
                    equalizer!!.enabled = true
                    stateSubject.onNext(EqInitializationState.INITIALIZED)
                }
            }
        }

        fun releaseEqualizer() {
            synchronized(this) {
                stateSubject.onNext(EqInitializationState.IDLE)
                if (equalizer != null) {
                    equalizer!!.enabled = false
                    equalizer!!.release()
                    equalizer = null
                    currentAudioSessionId = DEFAULT_AUDIO_SESSION_ID
                }
            }
        }

        fun <T> useEqualizer(func: (Equalizer) -> T): T {
            synchronized(this) {
                val equalizer: Equalizer?
                if (this.equalizer == null) {
                    equalizer = newEqualizer(0, currentAudioSessionId)
                    if (equalizer == null) {
                        throw EqInitializationException()
                    }
                } else {
                    equalizer = this.equalizer
                }
                return try {
                    func(equalizer!!)
                } catch (e: Exception) {
                    throw EqInitializationException(e)
                } finally {
                    if (this.equalizer == null) {
                        equalizer!!.release()
                    }
                }
            }
        }

        fun getEqInitializationState(): Observable<EqInitializationState> = stateSubject

        fun getEqualizer() = equalizer

        private fun newEqualizer(priority: Int, audioSession: Int): Equalizer? {
            var ex: Throwable? = null
            for (i in 0 until EQ_RETRY_CALLS_COUNT) {
                try {
                    return Equalizer(priority, audioSession)
                } catch (e: RuntimeException) {
                    ex = e
                }
            }
            onInitializationError(ex!!)
            return null
        }

        companion object {
            private const val EQ_RETRY_CALLS_COUNT = 3
            private const val DEFAULT_AUDIO_SESSION_ID = 1
            private const val EQ_PRIORITY = 1000

            //let's try several times, will see how it will work
            fun setBandLevel(eq: Equalizer, key: Short, value: Short) {
                var ex: RuntimeException? = null
                for (i in 0 until EQ_RETRY_CALLS_COUNT) {
                    ex = try {
                        eq.setBandLevel(key, value)
                        return
                    } catch (e: RuntimeException) {
                        e
                    }
                }
                throw ex!!
            }
        }
    }
}