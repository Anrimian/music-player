package com.github.anrimian.musicplayer.ui.utils.compose

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** Default window (ms) within which repeated clicks are ignored. */
const val DEFAULT_CLICK_DEBOUNCE_MS = 500L

/**
 * Wraps [onClick] in a time-based debounce and returns a stable callback.
 *
 * The first click fires immediately; subsequent clicks within [debounceMs] are dropped. Use this for
 * non-idempotent one-shot actions that must not run twice on a fast double-tap — navigation, one-shot
 * writes, etc. Works with any API that takes an `onClick: () -> Unit` (Material `Button`, `IconButton`,
 * `Modifier.clickable`, list-item callbacks, ...).
 *
 * NOTE: dialog-opening clicks do NOT need this — duplicate dialogs are already prevented centrally by
 * `BaseViewModel.showDialog`.
 */
@Composable
fun rememberDebouncedClick(
    debounceMs: Long = DEFAULT_CLICK_DEBOUNCE_MS,
    onClick: () -> Unit,
): () -> Unit {
    val lastClickTime = remember { mutableLongStateOf(0L) }
    val currentOnClick by rememberUpdatedState(onClick)
    return remember(debounceMs) {
        {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime.longValue >= debounceMs) {
                lastClickTime.longValue = now
                currentOnClick()
            }
        }
    }
}

/**
 * Like [Modifier.clickable] but ignores repeated clicks within [debounceMs]. Convenience wrapper around
 * [rememberDebouncedClick] for the `Modifier.clickable { }` call sites.
 */
fun Modifier.clickableDebounced(
    debounceMs: Long = DEFAULT_CLICK_DEBOUNCE_MS,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val debounced = rememberDebouncedClick(debounceMs, onClick)
    this.clickable(enabled = enabled, onClick = debounced)
}
