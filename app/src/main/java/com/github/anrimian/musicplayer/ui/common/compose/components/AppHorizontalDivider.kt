package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.anrimian.musicplayer.ui.common.compose.Dimens

@Composable
fun AppHorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = Dimens.dividerThickness
    )
}