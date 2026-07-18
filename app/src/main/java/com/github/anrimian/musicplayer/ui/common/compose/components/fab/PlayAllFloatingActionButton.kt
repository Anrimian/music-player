package com.github.anrimian.musicplayer.ui.common.compose.components.fab

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.R

@Composable
fun PlayAllFloatingActionButton(
    isRandomEnabled: Boolean,
    visible: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = if (isRandomEnabled) R.drawable.ic_shuffle else R.drawable.ic_play
    val descriptionRes = if (isRandomEnabled) R.string.shuffle_all_and_play else R.string.play_all

    AppFloatingActionButton(
        onClick = onClick,
        onLongClick = onLongClick,
        painter = painterResource(id = iconRes),
        contentDescription = stringResource(descriptionRes),
        visible = visible,
        modifier = modifier
    )
}