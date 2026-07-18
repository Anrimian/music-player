package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.contentTitle

@Composable
fun ContentTitle(label: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(
            top = Dimens.contentVerticalMargin,
            start = Dimens.contentHorizontalMargin,
            end = Dimens.contentHorizontalMargin
        ),
        text = label,
        style = MaterialTheme.typography.contentTitle,
    )
}