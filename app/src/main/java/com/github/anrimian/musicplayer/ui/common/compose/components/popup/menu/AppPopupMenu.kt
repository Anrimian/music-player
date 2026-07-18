package com.github.anrimian.musicplayer.ui.common.compose.components.popup.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.LocalDeviceCapabilities
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.components.popup.AppPopupPagerWindow
import com.github.anrimian.musicplayer.ui.common.compose.components.popup.AppPopupWindow
import com.github.anrimian.musicplayer.ui.common.compose.components.popup.HorizontalStrategy
import com.github.anrimian.musicplayer.ui.common.compose.components.popup.VerticalStrategy
import com.github.anrimian.musicplayer.ui.common.compose.medium
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.utils.compose.partitionToImmutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AppPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    menuItems: ImmutableList<AppMenuItem>,
    itemClickListener: (AppMenuItem) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: (@Composable (Modifier) -> Unit)? = null,
    offset: DpOffset = DpOffset(0.dp, (-8).dp),
    cornerRadius: Dp = 8.dp,
    verticalPadding: Dp = 8.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val (primaryItems, secondaryItems) = remember(menuItems) {
        val (first, second) = menuItems.partitionToImmutable { item -> item.groupId == 0 }

        if (first.isEmpty() && second.isNotEmpty()) {
            second to persistentListOf()
        } else {
            first to second
        }
    }

    val hasSecondary = secondaryItems.isNotEmpty()
            && LocalDeviceCapabilities.current.isResizeablePopupsSupported

    AppPopupWindow(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        horizontalStrategy = HorizontalStrategy.StartOutside,
        verticalStrategy = VerticalStrategy.TopAligned,
        offset = offset,
        containerColor = containerColor,
        cornerRadius = cornerRadius
    ) {
        if (hasSecondary) {
            AppPopupPagerWindow(
                primaryContent = { onNextPage ->
                    MenuColumnContent(
                        headerContent = headerContent,
                        items = primaryItems,
                        itemClickListener = itemClickListener,
                        onDismiss = onDismissRequest,
                        onHeaderClick = onNextPage,
                        verticalPadding = verticalPadding
                    )
                },
                secondaryContent = { onBack ->
                    MenuColumnContent(
                        headerContent = { headerModifier -> SecondaryMenuHeader(headerModifier) },
                        items = secondaryItems,
                        itemClickListener = itemClickListener,
                        onDismiss = onDismissRequest,
                        onHeaderClick = onBack,
                        verticalPadding = verticalPadding
                    )
                },
                secondaryContainerColor = containerColor,
                cornerRadius = cornerRadius
            )
        } else {
            MenuColumnContent(
                headerContent = headerContent,
                items = primaryItems,
                itemClickListener = itemClickListener,
                onDismiss = onDismissRequest,
                onHeaderClick = null,
                verticalPadding = verticalPadding
            )
        }
    }
}


@Composable
private fun MenuColumnContent(
    headerContent: (@Composable (Modifier) -> Unit)?,
    items: ImmutableList<AppMenuItem>,
    itemClickListener: (AppMenuItem) -> Unit,
    onDismiss: () -> Unit,
    onHeaderClick: (() -> Unit)?,
    verticalPadding: Dp
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = verticalPadding)
            .width(IntrinsicSize.Max)
    ) {
        if (headerContent == null) {
            Spacer(Modifier.height(verticalPadding))
        } else {
            val headerModifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onHeaderClick != null) {
                        Modifier.clickable(onClick = onHeaderClick)
                    } else {
                        Modifier
                    }
                )
            headerContent(headerModifier)
        }

        items.forEach { item ->
            PopupMenuItem(
                item = item,
                itemClickListener = itemClickListener,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun PopupMenuItem(
    item: AppMenuItem,
    itemClickListener: (AppMenuItem) -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                itemClickListener(item)
                onDismiss()
            }
            .padding(horizontal = 16.dp, vertical = Dimens.listVerticalMargin),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val contentColor = MaterialTheme.colorScheme.onSurface
        val title = item.title.asString()
        if (item.iconRes != null) {
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SecondaryMenuHeader(modifier: Modifier) {
    Column {
        Row(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.back),
                style = MaterialTheme.typography.titleMedium
            )
        }
        AppHorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
    }
}