package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import android.content.ClipData
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.clipboard.CopyToClipboard
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.PlayerScreenScaffold
import com.github.anrimian.musicplayer.ui.common.compose.components.progress.ProgressState
import com.github.anrimian.musicplayer.ui.common.compose.components.snackbar.AppSnackbarHost
import com.github.anrimian.musicplayer.ui.common.compose.medium
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.generateHarmoniousColor
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.FocusLyricsPart
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.LyricsLine
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.LyricsParser
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.LyricsParser.findTimePart
import com.github.anrimian.musicplayer.ui.utils.compose.Ref
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import com.github.anrimian.musicplayer.ui.utils.compose.awaitItemsPresence
import com.github.anrimian.musicplayer.ui.utils.compose.getValue
import com.github.anrimian.musicplayer.ui.utils.compose.setValue
import com.github.anrimian.musicplayer.ui.utils.compose.windowedScrollToPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val INTERACTION_SCROLL_DELAY_MILLIS = 3000L


@Composable
fun LyricsScreen(
    viewModel: LyricsViewModel,
    navigationCallback: (CommonEffect.NavigationEffect) -> Unit,
    actionsCallback: (BaseEffect) -> Unit,
    menuStateCallback: (Boolean) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    ObserveEffects(
        flow = viewModel.effects,
        snackbarHostState = snackbarHostState,
        onNavigation = navigationCallback,
        onEffect = { effect ->
            when (effect) {
                is CopyToClipboard -> {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("lyrics", effect.text)))
                    }
                }
                else -> actionsCallback(effect)
            }
        }
    )

    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isEditLyricsEnabled) {
        menuStateCallback(state.isEditLyricsEnabled)
    }

    LyricsScreenContent(
        state = state,
        listState = listState,
        onEditLyricsClicked = viewModel::onEditLyricsClicked,
        onWordClicked = viewModel::onWordClicked,
        onLineLongClicked = viewModel::onLineLongClicked,
        snackbarHost = { AppSnackbarHost(snackbarHostState) }
    )
}

@Composable
private fun LyricsScreenContent(
    state: LyricsState,
    listState: LazyListState,
    onEditLyricsClicked: () -> Unit,
    onWordClicked: (Long) -> Unit,
    onLineLongClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    PlayerScreenScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = snackbarHost,
    ) {
        ProgressState(
            state = state.lyrics,
            emptyAction = if (state.isEditLyricsEnabled) onEditLyricsClicked else null,
            emptyActionText = stringResource(R.string.edit_lyrics)
        ) { lyrics ->
            val currentPart = state.currentLyricsPart
            val currentCompositionId = state.currentCompositionId

            LaunchedEffect(currentCompositionId) {
                if (!lyrics.isHighlightAvailable && lyrics.lines.isNotEmpty()) {
                    listState.scrollToItem(0)
                }
            }

            var tappedLineIndexToSkip by remember(currentCompositionId) { Ref<Int?>(-1) }
            var isUserInteracting by remember(currentCompositionId) { Ref(false) }
            LaunchedEffect(listState.interactionSource, currentCompositionId) {
                val interactions = mutableListOf<DragInteraction.Start>()
                listState.interactionSource.interactions.collectLatest { interaction ->
                    when (interaction) {
                        is DragInteraction.Start -> interactions.add(interaction)
                        is DragInteraction.Stop -> interactions.remove(interaction.start)
                        is DragInteraction.Cancel -> interactions.remove(interaction.start)
                    }
                    if (interactions.isNotEmpty()) {
                        isUserInteracting = true
                    } else {
                        delay(INTERACTION_SCROLL_DELAY_MILLIS)
                        isUserInteracting = false
                    }
                }
            }

            val currentPartState = rememberUpdatedState(currentPart)
            val currentPartProvider: () -> FocusLyricsPart? = remember { { currentPartState.value } }
            val onWordClickedHandler = remember(currentCompositionId) {
                { index: Int, time: Long ->
                    tappedLineIndexToSkip = index
                    onWordClicked(time)
                }
            }

            val currentLineIndex = currentPart?.lineIndex
            var lastScrolledLineIndex by remember(currentCompositionId) { Ref<Int?>(-2) }
            LaunchedEffect(currentLineIndex) {
                if (currentLineIndex == null ||
                    currentLineIndex == -1 ||
                    currentLineIndex == lastScrolledLineIndex
                ) {
                    return@LaunchedEffect
                }

                listState.awaitItemsPresence()

                val isFirstScroll = lastScrolledLineIndex == -2
                lastScrolledLineIndex = currentLineIndex

                if (isUserInteracting || listState.isScrollInProgress) return@LaunchedEffect

                val isTappedLine = currentLineIndex == tappedLineIndexToSkip
                tappedLineIndexToSkip = -1

                if (isTappedLine) return@LaunchedEffect

                listState.windowedScrollToPosition(
                    position = currentLineIndex,
                    windowBottomRatio = 0.6f,
                    isSnapRequested = isFirstScroll,
                    animateInvisible = true
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    vertical = Dimens.contentVerticalMargin,
                    horizontal = Dimens.contentHorizontalMargin
                )
            ) {
                itemsIndexed(
                    items = lyrics.lines,
                    key = { index, _ -> index },
                    contentType = { _, _ -> "LyricLine" }
                ) { lineIndex, line ->
                    LyricLineItem(
                        line = line,
                        lineIndex = lineIndex,
                        currentPartProvider = currentPartProvider,
                        onWordClicked = onWordClickedHandler,
                        onLineLongClicked = onLineLongClicked,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricLineItem(
    line: LyricsLine,
    lineIndex: Int,
    currentPartProvider: () -> FocusLyricsPart?,
    onWordClicked: (Int, Long) -> Unit,
    onLineLongClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val baseHighlightColor = remember(primaryColor, line.roleIndex) {
        primaryColor.generateHarmoniousColor(line.roleIndex)
    }
    val currentHighlightColor = remember(baseHighlightColor) { baseHighlightColor.copy(alpha = 0.4f) }
    val nextHighlightColor = remember(baseHighlightColor) { baseHighlightColor.copy(alpha = 0.2f) }

    val annotatedString = remember(line) {
        buildAnnotatedString {
            line.words.forEach { word ->
                append(word.text)
            }
        }
    }

    var textLayoutResult by remember { Ref<TextLayoutResult?>(null) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.medium,
            textAlign = TextAlign.Center,
            onTextLayout = { result -> textLayoutResult = result },
            modifier = Modifier
                .pointerInput(line.words) {
                    detectTapGestures(
                        onLongPress = {
                            onLineLongClicked(annotatedString.text)
                        },
                        onTap = { pos ->
                            val layoutResult = textLayoutResult ?: return@detectTapGestures
                            val offset = layoutResult.getOffsetForPosition(pos)
                            val word = line.words.find { word ->
                                offset in word.charStartIndex until word.charEndIndex
                            }
                            if (word != null && word.timeStart != -1L) {
                                onWordClicked(lineIndex, word.timeStart)
                            }
                        }
                    )
                }
                .drawBehind {
                    val currentPart = currentPartProvider()
                    if (currentPart == null || currentPart.lineIndex != lineIndex) return@drawBehind
                    val layoutResult = textLayoutResult ?: return@drawBehind
                    val activeWord = line.words.getOrNull(currentPart.wordIndex) ?: return@drawBehind

                    val color = if (currentPart.isFocused) currentHighlightColor else nextHighlightColor
                    val path = layoutResult.getPathForRange(activeWord.charStartIndex, activeWord.trimmedCharEndIndex)
                    val bounds = path.getBounds()
                    val paddingX = 3.sp.toPx()
                    val paddingY = 0.sp.toPx()
                    val radius = 6.dp.toPx()

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(bounds.left - paddingX, bounds.top - paddingY),
                        size = Size(bounds.width + paddingX * 2, bounds.height + paddingY * 2),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
        )
    }
}


//region Previews

@Preview
@Composable
private fun LyricsScreenContentPreview() {
    val rawLyrics = """
        [00:00.00]by RentAnAdviser.com
        [00:36.60]Manches sollte, manches nicht
        [00:40.60]Wir sehen, doch sind wir blind
        [00:44.50]Wir werfen Schatten ohne Licht
        [00:52.40]Nach uns wird es vorher geben
        [00:56.40]Aus der Jugend wird schon Not
        [01:00.10]Wir sterben weiter, bis wir leben
        [01:03.90]Sterben lebend in den Tod
        [01:07.80]Dem Ende treiben wir entgegen
        [01:11.80]Keine Rast, nur vorwärts streben
        [01:15.70]Am Ufer winkt Unendlichkeit
        [01:18.70]Gefangen so im Fluss dеr Zeit
        [01:26.60]Bitte bleib stеh'n, bleib steh'n
        [01:31.30]Zeit
        [01:33.80]Das soll always so weitergeh'n
    """.trimIndent()
    val lyricsText = LyricsParser.parseLyrics(rawLyrics, {})
    val currentPart = lyricsText.lines.findTimePart(42000, 100000)

    PreviewAppTheme {
        LyricsScreenContent(
            state = LyricsState(
                lyrics = StatedData.Content(lyricsText),
                currentLyricsPart = currentPart,
                isEditLyricsEnabled = false
            ),
            listState = rememberLazyListState(),
            onEditLyricsClicked = {},
            onWordClicked = {},
            onLineLongClicked = {}
        )
    }
}

@Preview
@Composable
private fun LyricsLoadingPreview() {
    PreviewAppTheme {
        LyricsScreenContent(
            state = LyricsState(
                lyrics = StatedData.Loading,
            ),
            listState = rememberLazyListState(),
            onEditLyricsClicked = {},
            onWordClicked = {},
            onLineLongClicked = {}
        )
    }
}

@Preview
@Composable
private fun LyricsEmptyPreview() {
    PreviewAppTheme {
        LyricsScreenContent(
            state = LyricsState(
                lyrics = StatedData.Empty(UiText.StringResource(R.string.no_lyrics_for_current_composition)),
                isEditLyricsEnabled = true
            ),
            listState = rememberLazyListState(),
            onEditLyricsClicked = {},
            onWordClicked = {},
            onLineLongClicked = {}
        )
    }
}

@Preview
@Composable
private fun LyricsAdvancedContentPreview() {
    val rawLyrics = """
        [00:13.24]<00:13.24>When <00:13.44>you're <00:13.60>down <00:13.96>on <00:14.76>your <00:14.95>luck<00:15.24>
        [00:15.89]<00:15.89>I <00:16.12>take <00:16.39>them <00:16.59>hands <00:16.89>and <00:17.21>I <00:17.34>turn <00:17.54>it <00:17.68>up<00:18.03>
        [00:18.57]<00:18.57>When <00:18.74>you're <00:18.95>face <00:19.38>to <00:20.13>the <00:20.32>floor<00:20.78>
        [00:21.25]<00:21.25>I <00:21.44>turn <00:21.73>the <00:21.92>dial <00:22.27>turn <00:22.47>it <00:22.60>up <00:22.93>more<00:24.97>
    """.trimIndent()
    val lyricsText = LyricsParser.parseLyrics(rawLyrics, {})
    val currentPart = lyricsText.lines.findTimePart(16000, 30000)

    PreviewAppTheme {
        LyricsScreenContent(
            state = LyricsState(
                lyrics = StatedData.Content(lyricsText),
                currentLyricsPart = currentPart,
                isEditLyricsEnabled = false
            ),
            listState = rememberLazyListState(),
            onEditLyricsClicked = {},
            onWordClicked = {},
            onLineLongClicked = {}
        )
    }
}

//endregion


