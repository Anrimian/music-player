package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.utils.DialogViewModelContainer
import com.github.anrimian.musicplayer.domain.models.menu.MenuCategory
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.applyDisabledAlpha
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.models.MenuConfigListItem
import com.github.anrimian.musicplayer.ui.common.compose.contentTitle
import com.github.anrimian.musicplayer.ui.common.compose.medium
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.BaseDialog
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun MenuConfigDialog(
    data: MenuConfigDialogData,
    onDismiss: () -> Unit,
) {
    DialogViewModelContainer<MenuConfigViewModel>(data, onDismiss) { viewModel, dismiss ->
        val state by viewModel.state.collectAsStateWithLifecycle()

        ObserveEffects(viewModel.effects) { effect ->
            when (effect) {
                MenuConfigEffect.Close -> dismiss()
            }
        }

        MenuConfigDialogContent(
            state = state,
            onDismiss = dismiss,
            onMove = viewModel::onItemMove,
            onSave = viewModel::onSave,
            onReset = viewModel::onReset,
        )
    }
}

@Composable
private fun MenuConfigDialogContent(
    state: MenuConfigState,
    onDismiss: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {

    BaseDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.configure_menu_items),
        positiveText = stringResource(R.string.save),
        positiveAction = onSave,
        negativeText = stringResource(R.string.cancel),
        negativeAction = onDismiss,
        neutralText = stringResource(R.string.reset),
        neutralAction = onReset,
        contentPadding = PaddingValues(0.dp),
    ) {
        ReorderableMenuList(
            items = state.items,
            onMove = onMove
        )
    }
}

@Composable
private fun ReorderableMenuList(
    items: List<MenuConfigListItem>,
    onMove: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState()

    val lockedItemId by remember(items) {
        derivedStateOf {
            val secondaryHeaderIndex = items.indexOfFirst { item ->
                item is MenuConfigListItem.Header && item.category == MenuCategory.SECONDARY
            }
            // primary header is on 0, if 2 - then we have primary header + item
            if (secondaryHeaderIndex == 2) {
                (items[1] as MenuConfigListItem.Item).key
            } else {
                null
            }
        }
    }

    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        onMove(from.index, to.index)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(
            items,
            key = { _, item -> item.key }
        ) { index, item ->

            val isItem = item is MenuConfigListItem.Item
            val isDraggable = isItem && item.key != lockedItemId

            ReorderableItem(
                state = reorderableState,
                key = item.key,
                enabled = index != 0
            ) { isDragging ->

                val elevation by animateDpAsState(if (isItem && isDragging) 4.dp else 0.dp)
                val itemBackground = MaterialTheme.colorScheme.surfaceContainerHigh

                when (item) {
                    is MenuConfigListItem.Header -> {
                        Text(
                            modifier = Modifier
                                .padding(
                                    top = Dimens.contentVerticalMargin,
                                    bottom = Dimens.contentSpacingVerticalMargin,
                                    end = Dimens.dialogContentHorizontalPadding,
                                    start = Dimens.dialogContentHorizontalPadding
                                )
                                .background(itemBackground),
                            text = stringResource(item.titleRes),
                            style = MaterialTheme.typography.contentTitle,
                        )
                    }
                    is MenuConfigListItem.Item -> {
                        MenuItem(
                            item = item.appMenuItem,
                            isDragEnabled = isDraggable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation)
                                .background(itemBackground),
                            dragHandleModifier = Modifier.draggableHandle()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    item: AppMenuItem,
    isDragEnabled: Boolean,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
    val iconPadding = 12.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.dialogContentHorizontalPadding,
                end = Dimens.dialogContentHorizontalPadding - iconPadding
            ),
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

        Icon(
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = null,
            tint = contentColor.applyDisabledAlpha(isDragEnabled),
            modifier = Modifier
                .padding(iconPadding)
                .then(if (isDragEnabled) dragHandleModifier else Modifier)
        )
    }
}