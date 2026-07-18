package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.ui.common.compose.components.popup.menu.AppPopupMenu
import com.github.anrimian.musicplayer.ui.common.compose.components.popup.menu.composition.CompositionPopupMenu
import com.github.anrimian.musicplayer.ui.common.compose.onSurfaceIcon
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MenuButton(
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(end = 4.dp, top = 2.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dots_vertical),
            contentDescription = stringResource(R.string.content_description_menu),
            tint = MaterialTheme.colorScheme.onSurfaceIcon
        )
    }
}

@Composable
fun PopupMenuButton(
    menuItems: ImmutableList<AppMenuItem>,
    onItemClick: (AppMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        MenuButton(
            onClick = { isExpanded = true }
        )

        AppPopupMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            menuItems = menuItems,
            itemClickListener = onItemClick,
        )
    }
}

@Composable
fun CompositionPopupMenuButton(
    composition: CompositionModel,
    menuItems: ImmutableList<AppMenuItem>,
    onItemClick: (AppMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        MenuButton(
            onClick = { isExpanded = true }
        )

        CompositionPopupMenu(
            composition = composition,
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            menuItems = menuItems,
            itemClickListener = onItemClick,
        )
    }
}