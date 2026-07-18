package com.github.anrimian.musicplayer.ui.common.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand

@Composable
fun ErrorCommand.format(@StringRes templateId: Int): String {
    return stringResource(templateId, message)
}