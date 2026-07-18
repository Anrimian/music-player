package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.common.DeviceCapabilities
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.ui.common.clipboard.CopyToClipboard
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.BaseViewModel
import com.github.anrimian.musicplayer.ui.common.mvvm.EmptyPersistent
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.LyricsParser
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.LyricsParser.findTimePart
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow

class LyricsViewModel(
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser,
    private val deviceCapabilities: DeviceCapabilities
): BaseViewModel<LyricsState, EmptyPersistent>(
    LyricsState(),
    EmptyPersistent,
    savedStateHandle,
    errorParser
) {

    private var currentComposition: Composition? = null
    private var trackPosition: Long = -1L

    init {
        libraryPlayerInteractor.getCurrentQueueItemObservable().asFlow()
            .subscribe(onNext = this::onCurrentQueueItemReceived)

        subscribeOnLyrics()
        subscribeOnTrackPosition()
    }

    fun onEditLyricsClicked() {
        val composition = currentComposition
        if (composition != null) {
            sendNavigationEffect(Screen.LyricsEditor(composition.id))
        }
    }

    fun onWordClicked(time: Long) {
        libraryPlayerInteractor.onSeekFinished(time)
    }

    fun onLineLongClicked(text: String) {
        sendEffect(CopyToClipboard(text))
        if (!deviceCapabilities.isClipboardVisualConfirmationSupported) {
            sendMessage(R.string.copied_message)
        }
    }

    fun onSleepTimerClicked() {
        sendEffect(LyricsEffect.ShowSleepTimerDialog)
    }

    fun onEqualizerClicked() {
        sendEffect(LyricsEffect.ShowEqualizerDialog)
    }

    private fun onCurrentQueueItemReceived(event: PlayQueueEvent) {
        val newComposition = event.playQueueItem
        if (currentComposition?.id != newComposition?.id) {
            this.trackPosition = -1L
            updateState {
                copy(
                    currentLyricsPart = null,
                    isEditLyricsEnabled = newComposition != null,
                    currentCompositionId = newComposition?.id
                )
            }
        }
        currentComposition = newComposition
        updateCurrentLyricsPart()
    }

    private fun subscribeOnLyrics() {
        libraryPlayerInteractor.getCurrentCompositionLyrics().asFlow()
            .map { lyricsOpt ->
                val lyricsText = lyricsOpt.value
                if (lyricsText == null) {
                    StatedData.Empty(UiText.StringResource(R.string.no_current_composition))
                } else {
                    val lyrics = LyricsParser.parseLyrics(lyricsText, errorParser::logError)
                    if (lyrics.lines.isEmpty()) {
                        StatedData.Empty(UiText.StringResource(R.string.no_lyrics_for_current_composition))
                    } else {
                        StatedData.Content(lyrics)
                    }
                }
            }
            .subscribeStated { lyrics ->
                updateState {
                    copy(
                        lyrics = lyrics,
                        isEditLyricsEnabled = currentComposition != null,
                        currentLyricsPart = null
                    )
                }
                updateCurrentLyricsPart()
            }
    }

    private fun subscribeOnTrackPosition() {
        state
            .distinctUntilChanged { old, new ->
                val oldHasLyrics = old.lyrics.data != null
                val newHasLyrics = new.lyrics.data != null
                oldHasLyrics == newHasLyrics && old.currentCompositionId == new.currentCompositionId
            }
            .flatMapLatest { state ->
                val hasLyrics = state.lyrics.data != null
                if (hasLyrics) {
                    libraryPlayerInteractor.getTrackPositionObservable().asFlow()
                } else {
                    emptyFlow()
                }
            }
            .subscribe { trackPosition ->
                this.trackPosition = trackPosition
                updateCurrentLyricsPart()
            }
    }

    private fun updateCurrentLyricsPart() {
        val composition = currentComposition
        val lyrics = currentState.lyrics.data
        if (composition == null || composition.duration == 0L || lyrics == null || trackPosition == -1L) {
            return
        }

        val currentPart = currentState.currentLyricsPart
        if (currentPart != null) {
            val line = lyrics.lines.getOrNull(currentPart.lineIndex)
            if (line != null) {
                val word = if (currentPart.wordIndex >= 0) line.words.getOrNull(currentPart.wordIndex) else null
                val timeStart = word?.timeStart ?: line.timeStart
                val duration = word?.duration ?: line.duration
                if (currentPart.isFocused) {
                    if (duration == -1L) {
                        // Unknown duration — stay on this part until position moves before its start
                        if (trackPosition >= timeStart) {
                            return
                        }
                    } else {
                        val partEndTime = timeStart + duration
                        if (trackPosition in (timeStart + 1)..<partEndTime) {
                            return
                        }
                    }
                }
            }
        }
        val lines = lyrics.lines
        val newPart = lines.findTimePart(trackPosition, composition.duration)

        if (newPart != currentState.currentLyricsPart) {
            updateState { copy(currentLyricsPart = newPart) }
        }
    }

}
