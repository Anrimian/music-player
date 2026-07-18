package com.github.anrimian.musicplayer.ui.settings.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.medium
import com.github.anrimian.musicplayer.ui.common.compose.settingsItemTitle
import com.github.anrimian.musicplayer.ui.common.compose.subtitle


@Composable
fun SettingsSingleTextItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.settingsItemTitle,
            modifier = Modifier.padding(
                start = Dimens.contentHorizontalMargin,
                top = Dimens.contentVerticalMargin,
                bottom = Dimens.contentVerticalMargin,
                end = Dimens.contentHorizontalMargin)
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val handleLongClick: (() -> Unit)? = remember(onLongClick) {
        onLongClick ?: return@remember null
        {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongClick()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = handleLongClick
            )
            .padding(
                end = Dimens.contentHorizontalMargin,
                top = Dimens.contentVerticalMargin,
                bottom = Dimens.contentVerticalMargin
            )
    ) {
        Icon(
            modifier = Modifier
                .padding(
                    top = Dimens.contentVerticalMargin,
                    start = Dimens.settingsIconHorizontalPadding,
                    end = Dimens.settingsIconHorizontalPadding
                ),
            painter = icon,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = title,
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.medium
            )
            Spacer(Modifier.height(Dimens.contentSpacingVerticalMargin))
            Text(
                text = description,
                style = MaterialTheme.typography.subtitle
            )
        }
    }
}

@Composable
fun SettingsTextItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimens.contentHorizontalMargin,
                vertical = Dimens.contentVerticalMargin
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.medium
        )
        Spacer(Modifier.height(Dimens.contentSpacingVerticalMargin))
        Text(
            text = description,
            style = MaterialTheme.typography.subtitle
        )
    }
}

@Composable
fun SettingsDivider() {
    AppHorizontalDivider(
        modifier = Modifier.padding(start = Dimens.contentHorizontalMargin),
    )
}
