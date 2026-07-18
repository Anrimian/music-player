package com.github.anrimian.musicplayer.ui.common.compose.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.platform.AccessibilityManager
import com.github.anrimian.musicplayer.ui.common.compose.text.UiTextResolver
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.MessageAction
import com.github.anrimian.musicplayer.ui.common.effects.MessageDuration
import kotlinx.coroutines.withTimeoutOrNull

private const val DURATION_SHORT_MS = 4000L
private const val DURATION_LONG_MS = 10000L

suspend fun SnackbarHostState.showCommonMessage(
    event: CommonEffect.ShowMessage,
    resolver: UiTextResolver,
    accessibilityManager: AccessibilityManager?,
    onAction: (MessageAction) -> Unit = {},
): SnackbarResult {
    with(resolver) {
        val messageText = event.message.resolve()
        val actionLabelText = event.actionLabel?.resolve()

        val rawDurationMillis = when (val d = event.duration) {
            MessageDuration.SystemShort -> DURATION_SHORT_MS
            MessageDuration.SystemLong -> DURATION_LONG_MS
            is MessageDuration.Custom -> d.millis
            MessageDuration.Indefinite -> Long.MAX_VALUE
        }

        val durationMillis = if (rawDurationMillis == Long.MAX_VALUE) {
            Long.MAX_VALUE
        } else {
            accessibilityManager?.calculateRecommendedTimeoutMillis(
                originalTimeoutMillis = rawDurationMillis,
                containsIcons = true,
                containsText = true,
                containsControls = event.actionLabel != null
            ) ?: rawDurationMillis
        }

        val showProgress = event.actionLabel != null && durationMillis != Long.MAX_VALUE

        val visuals = AppSnackbarVisuals(
            message = messageText,
            actionLabel = actionLabelText,
            duration = SnackbarDuration.Indefinite,
            durationMillis = durationMillis,
            showProgressBar = showProgress
        )

        val result = if (durationMillis == Long.MAX_VALUE) {
            showSnackbar(visuals)
        } else {
            withTimeoutOrNull(durationMillis) {
                showSnackbar(visuals)
            } ?: SnackbarResult.Dismissed
        }

        if (result == SnackbarResult.ActionPerformed) {
            event.action?.let(onAction)
        }

        return result
    }
}

data class AppSnackbarVisuals(
    override val message: String,
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val withDismissAction: Boolean = false,
    val durationMillis: Long,
    val showProgressBar: Boolean
) : SnackbarVisuals