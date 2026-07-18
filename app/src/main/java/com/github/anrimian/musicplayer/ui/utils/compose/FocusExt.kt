package com.github.anrimian.musicplayer.ui.utils.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

fun Modifier.requestKeyboardFocus(): Modifier = composed {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var hasFocusBeenRequested by remember { mutableStateOf(false) }

    this.focusRequester(focusRequester)
        .onGloballyPositioned {
            if (!hasFocusBeenRequested) {
                focusRequester.requestFocus()
                keyboardController?.show()
                hasFocusBeenRequested = true
            }
        }
}