package com.github.anrimian.musicplayer.ui.playlists.details

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import com.github.anrimian.musicplayer.domain.models.utils.isFileRemote
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppCoverImage
import com.github.anrimian.musicplayer.ui.common.compose.components.AppFastScroller
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.components.AppScaffold
import com.github.anrimian.musicplayer.ui.common.compose.components.CompositionPopupMenuButton
import com.github.anrimian.musicplayer.ui.common.compose.components.PlayPauseIcon
import com.github.anrimian.musicplayer.ui.common.compose.components.TextWithSeparators
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.ConfirmDeletePlaylistDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.ConfirmDeletePlaylistDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.compositions.ConfirmDeleteCompositionsDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.compositions.ConfirmDeleteCompositionsDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.MenuConfigDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.MenuConfigDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.fab.PlayAllFloatingActionButton
import com.github.anrimian.musicplayer.ui.common.compose.components.fab.rememberScrollingFabVisibility
import com.github.anrimian.musicplayer.ui.common.compose.components.fsync.FileSyncStateIcon
import com.github.anrimian.musicplayer.ui.common.compose.components.progress.ProgressState
import com.github.anrimian.musicplayer.ui.common.compose.components.snackbar.AppSnackbarHost
import com.github.anrimian.musicplayer.ui.common.compose.contentSubtitle
import com.github.anrimian.musicplayer.ui.common.compose.dragContainer
import com.github.anrimian.musicplayer.ui.common.compose.itemPrimary
import com.github.anrimian.musicplayer.ui.common.compose.playingContainer
import com.github.anrimian.musicplayer.ui.common.delete.ShowDeleteErrorEffect
import com.github.anrimian.musicplayer.ui.common.delete.rememberDeleteErrorResolver
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.format.AppFormatUtils
import com.github.anrimian.musicplayer.ui.common.format.AppTimeFormatUtils
import com.github.anrimian.musicplayer.ui.common.lists.ScrollToPositionEffect
import com.github.anrimian.musicplayer.ui.common.models.fsync.UiFileSyncState
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.library.common.library.LibraryDialogHost
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialog
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogData
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlaylistDialog
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlaylistDialogData
import com.github.anrimian.musicplayer.ui.utils.compose.SearchScrollHandler
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import com.github.anrimian.musicplayer.ui.utils.compose.appItemAnimation
import com.github.anrimian.musicplayer.ui.utils.compose.attachStopCallback
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.MultiActionSwipeContainer
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseContainer
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.compose.navigationBarPaddingCompat
import com.github.anrimian.musicplayer.ui.utils.compose.rememberDragDropState
import com.github.anrimian.musicplayer.ui.utils.compose.scrollToPosition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun PlaylistDetailsScreen(
    viewModel: PlaylistDetailsViewModel,
    navigationCallback: (CommonEffect.NavigationEffect) -> Unit,
    actionsCallback: (PlaylistDetailsEffect) -> Unit,
    toolbarCallback: (Playlist) -> Unit,
    swipeToCloseReporter: SwipeToCloseReporter,
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    listState.attachStopCallback(viewModel::onStop)

    val deleteErrorResolver = rememberDeleteErrorResolver(
        onPermissionResult = viewModel::onDeletePermissionResult
    )

    ObserveEffects(
        flow = viewModel.effects,
        snackbarHostState = snackbarHostState,
        onMessageAction = { action ->
            when (action) {
                RestoreRemovedPlaylistEntry -> viewModel.onRestoreRemovedEntryClicked()
                UndoSortAction -> viewModel.onUndoSortButtonClicked()
            }
        },
        onNavigation = navigationCallback,
        onEffect = { effect ->
            when (effect) {
                is ScrollToPositionEffect -> {
                    listState.scrollToPosition(scope, effect.position)
                }
                is ShowDeleteErrorEffect -> {
                    deleteErrorResolver(effect)
                }
                is PlaylistDetailsEffect -> actionsCallback(effect)
            }
        }
    )

    LibraryDialogHost(viewModel) { dialog ->
        when (dialog) {
            is MenuConfigDialogData -> {
                MenuConfigDialog(
                    dialog,
                    onDismiss = viewModel::onMenuConfigDialogClosed,
                )
            }
            is ConfirmDeleteCompositionsDialogData -> {
                ConfirmDeleteCompositionsDialog(
                    dialog,
                    onConfirm = viewModel::onConfirmDeleteCompositionsDialogConfirmed,
                    onDismiss = viewModel::onConfirmDeleteCompositionsDialogDismissed
                )
            }
            is RenamePlaylistDialogData -> {
                RenamePlaylistDialog(
                    data = dialog,
                    onDismiss = viewModel::onRenamePlaylistDialogClosed
                )
            }
            is ConfirmDeletePlaylistDialogData -> {
                ConfirmDeletePlaylistDialog(
                    data = dialog,
                    onConfirm = viewModel::onDeletePlaylistDialogConfirmed,
                    onDismiss = viewModel::onConfirmDeletePlaylistDialogClosed
                )
            }
            is SelectOrderDialogData -> {
                SelectOrderDialog(
                    data = dialog,
                    onDismiss = viewModel::onSortDialogDismissed,
                    onOrderSelected = viewModel::onSortSelected
                )
            }
        }
    }

    SwipeToCloseContainer(
        modifier = Modifier.navigationBarPaddingCompat(),
        onDismiss = swipeToCloseReporter::onDismiss,
        onDragProgress = swipeToCloseReporter::onDragProgress
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()

        val playList = state.playlist
        LaunchedEffect(playList) {
            if (playList != null) {
                toolbarCallback(playList)
            }
        }

        PlaylistDetailsScreenContent(
            state = state,
            listState = listState,
            onPlayAllButtonClicked = viewModel::onPlayAllButtonClicked,
            onChangeRandomModeClicked = viewModel::onChangeRandomModeClicked,
            onTryAgainButtonClicked = viewModel::onTryAgainButtonClicked,
            onItemClicked = viewModel::onItemClicked,
            onPlaylistEntryMenuItemClicked = viewModel::onPlaylistEntryMenuItemClicked,
            onItemMoved = viewModel::onItemMoved,
            onItemDragEnded = viewModel::onItemDragEnded,
            onItemSwipedToPlayNext = viewModel::onItemSwipedToPlayNext,
            onItemSwipedToDelete = viewModel::onItemSwipedToDelete,
            snackbarHost = { AppSnackbarHost(snackbarHostState) }
        )
    }
}

@Composable
private fun PlaylistDetailsScreenContent(
    state: PlaylistDetailsState,
    listState: LazyListState,
    onPlayAllButtonClicked: () -> Unit,
    onChangeRandomModeClicked: () -> Unit,
    onTryAgainButtonClicked: () -> Unit,
    onItemClicked: (PlaylistEntry, Int) -> Unit,
    onPlaylistEntryMenuItemClicked: (AppMenuItem, PlaylistEntry, Int) -> Unit,
    onItemMoved: (Int, Int) -> Unit,
    onItemDragEnded: (Int, Int) -> Unit,
    onItemSwipedToPlayNext: (PlaylistEntry) -> Unit,
    onItemSwipedToDelete: (PlaylistEntry) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {

    val (isFabVisible, fabNestedScrollConnection) = rememberScrollingFabVisibility(listState)

    AppScaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(fabNestedScrollConnection),
        snackbarHost = snackbarHost,
        floatingActionButton = {
            if (state.playlistItems is StatedData.Content) {
                PlayAllFloatingActionButton(
                    isRandomEnabled = state.isRandomEnabled,
                    visible = isFabVisible.value,
                    onClick = onPlayAllButtonClicked,
                    onLongClick = onChangeRandomModeClicked
                )
            }
        },
    ) { innerPadding ->
        SearchScrollHandler(
            listState = listState,
            searchQuery = state.searchQuery,
            dataState = state.playlistItems,
            isContentReady = { dataState -> dataState is StatedData.Content },
            getItems = { dataState -> dataState.data }
        )

        ProgressState(
            state = state.playlistItems,
            contentPadding = innerPadding,
            errorAction = onTryAgainButtonClicked
        ) { playlistEntries ->
            val bottomPadding = innerPadding.calculateBottomPadding() + Dimens.bottomPaddingWithFab

            Box(modifier = Modifier.fillMaxSize()) {

                PlaylistEntriesList(
                    items = playlistEntries,
                    fileSyncStates = state.fileSyncStates,
                    listState = listState,
                    currentComposition = state.currentComposition,
                    isCoversEnabled = state.isCoversEnabled,
                    menuItems = state.menuItems,
                    onItemClick = onItemClicked,
                    onMenuClick = onPlaylistEntryMenuItemClicked,
                    onMove = onItemMoved,
                    onDragEnd = onItemDragEnded,
                    onItemSwipedToPlayNext = onItemSwipedToPlayNext,
                    onItemSwipedToDelete = onItemSwipedToDelete,
                    isDragEnabled = state.searchQuery.isNullOrEmpty(),
                    contentPadding = PaddingValues(
                        bottom = bottomPadding
                    )
                )

                AppFastScroller(
                    listState = listState,
                    contentPadding = PaddingValues(
                        top = Dimens.contentVerticalMargin,
                        bottom = bottomPadding + Dimens.contentVerticalMargin
                    ),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun PlaylistEntriesList(
    items: PersistentList<PlaylistEntry>,
    fileSyncStates: ImmutableMap<Long, UiFileSyncState>,
    listState: LazyListState,
    currentComposition: CurrentComposition?,
    isCoversEnabled: Boolean,
    menuItems: ImmutableList<AppMenuItem>,
    onItemClick: (PlaylistEntry, Int) -> Unit,
    onMenuClick: (AppMenuItem, PlaylistEntry, Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDragEnd: (Int, Int) -> Unit,
    onItemSwipedToPlayNext: (PlaylistEntry) -> Unit,
    onItemSwipedToDelete: (PlaylistEntry) -> Unit,
    isDragEnabled: Boolean,
    contentPadding: PaddingValues,
) {
    val dragDropState = rememberDragDropState(onMove = onMove, onDragEnd = onDragEnd)

    val haptic = LocalHapticFeedback.current

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to -> dragDropState.onMove(from, to) }
    )

    val playNextIcon = painterResource(R.drawable.ic_play_next)
    val playNextText = stringResource(R.string.play_next)
    val deleteIcon = painterResource(R.drawable.ic_playlist_remove)
    val deleteText = stringResource(R.string.delete_from_play_list)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(
            items,
            key = { _, item -> item.entryId },
            contentType = { _, _ -> "playlist_entry" }
        ) { index, item ->

            ReorderableItem(
                state = reorderableState,
                key = item.entryId
            ) { isDragging ->
                LaunchedEffect(isDragging) {
                    if (isDragging) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    dragDropState.onDragStateChanged(isDragging)
                }

                val elevation by animateDpAsState(if (isDragging) 1.dp else 0.dp)

                Column(
                    modifier = Modifier
                        .appItemAnimation(this@itemsIndexed, isDragging)
                        .shadow(elevation)
                        .longPressDraggableHandle(enabled = isDragEnabled)
                ) {
                    val onFirstAction = remember(item) { { onItemSwipedToPlayNext(item) } }
                    val onSecondAction = remember(item) { { onItemSwipedToDelete(item) } }

                    MultiActionSwipeContainer(
                        firstActionIcon = playNextIcon,
                        firstActionText = playNextText,
                        onFirstAction = onFirstAction,
                        secondActionIcon = deleteIcon,
                        secondActionText = deleteText,
                        onSecondAction = onSecondAction
                    ) {
                        val isCurrent = item.id == currentComposition?.composition?.id
                        val isPlaying = isCurrent && currentComposition.isPlaying
                        val fileSyncState = fileSyncStates[item.id]

                        val onItemClickAction = remember(item, index) { { onItemClick(item, index) } }
                        val onMenuClickAction = remember(item, index) { { menuItem: AppMenuItem -> onMenuClick(menuItem, item, index) } }

                        PlaylistItem(
                            item = item,
                            fileSyncState = fileSyncState,
                            isCurrent = isCurrent,
                            isPlaying = isPlaying,
                            isCoversEnabled = isCoversEnabled,
                            menuItems = menuItems,
                            isDragging = isDragging,
                            onItemClick = onItemClickAction,
                            onMenuClick = onMenuClickAction
                        )
                    }


                    if (!isDragging) {
                        val dividerIndent = Dimens.contentHorizontalMargin +
                                Dimens.coverImageListItemSize +
                                Dimens.contentHorizontalMargin
                        AppHorizontalDivider(
                            modifier = Modifier.padding(start = dividerIndent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    item: PlaylistEntry,
    fileSyncState: UiFileSyncState?,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isCoversEnabled: Boolean,
    menuItems: ImmutableList<AppMenuItem>,
    isDragging: Boolean,
    onItemClick: () -> Unit,
    onMenuClick: (AppMenuItem) -> Unit,
) {
    val targetSelectionOverlayColor = when {
        isDragging -> MaterialTheme.colorScheme.dragContainer
        isCurrent -> MaterialTheme.colorScheme.playingContainer
        else -> Color.Transparent
    }

    val selectionOverlayColor by animateColorAsState(
        targetValue = targetSelectionOverlayColor,
        animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
        label = "selectionAnimation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .background(selectionOverlayColor)
            .clickable { onItemClick() }
            .padding(start = Dimens.contentHorizontalMargin)
    ) {

        Row(
            modifier = Modifier
                .padding(vertical = Dimens.listVerticalMargin)
                .weight(1f)

        ) {
            Box(contentAlignment = Alignment.Center) {
                AppCoverImage(
                    composition = item,
                    isCoversEnabled = isCoversEnabled,
                    modifier = Modifier.size(Dimens.coverImageListItemSize)
                )

                PlayPauseIcon(
                    isPlaying = isPlaying,
                    modifier = Modifier.size(24.dp)
                )

                val isFileRemote = remember(item) { item.isFileRemote() }
                FileSyncStateIcon(
                    fileSyncState = fileSyncState,
                    isFileRemote = isFileRemote,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }

            Spacer(Modifier.width(Dimens.contentHorizontalMargin))

            val alpha = if (item.corruptionType == null) 1f else 0.5f
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(alpha)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.itemPrimary,
                )

                Spacer(Modifier.height(Dimens.contentSpacingVerticalMargin))

                PlaylistEntryAdditionalInfo(
                    artist = item.artist,
                    duration = item.duration,
                    corruptionType = item.corruptionType
                )
            }
        }

        CompositionPopupMenuButton(
            composition = item,
            menuItems = menuItems,
            onItemClick = onMenuClick
        )
    }
}

@Composable
fun PlaylistEntryAdditionalInfo(
    artist: String?,
    duration: Long,
    corruptionType: CorruptionType?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.contentSubtitle,
) {
    val artistText = AppFormatUtils.formatArtist(artist)
    val durationText = remember(duration) { AppTimeFormatUtils.formatMilliseconds(duration) }
    val corruptionText = AppFormatUtils.formatCorruptionType(corruptionType)

    val errorColor = MaterialTheme.colorScheme.error

    val items = remember(artist, duration, corruptionType, errorColor) {
        buildList {
            add(AnnotatedString(artistText))
            add(AnnotatedString(durationText))
            if (corruptionText != null) {
                add(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = errorColor)) {
                            append(corruptionText)
                        }
                    }
                )
            }
        }
    }

    TextWithSeparators(
        items = items,
        modifier = modifier,
        style = style
    )
}

//region Previews

@Preview
@Composable
private fun PlaylistDetailsScreenContentPreview() {
    val items = persistentListOf(
        PlaylistEntry(
            entryId = 1,
            id = 1,
            title = "Composition 1",
            artist = "Artist 1",
            album = "Album 1",
            duration = 120000,
            size = 1000000,
            comment = null,
            storageId = 1,
            addedTime = 0,
            modifiedTime = 0,
            coverModifyTime = 0,
            fileStatus = LocalFileStatus.AVAILABLE,
            corruptionType = null,
            isFileExists = true,
            initialSource = InitialSource.LOCAL
        ),
        PlaylistEntry(
            entryId = 2,
            id = 2,
            title = "Composition 2",
            artist = "Artist 2",
            album = "Album 2",
            duration = 180000,
            size = 1500000,
            comment = null,
            storageId = 2,
            addedTime = 0,
            modifiedTime = 0,
            coverModifyTime = 0,
            fileStatus = LocalFileStatus.AVAILABLE,
            corruptionType = null,
            isFileExists = true,
            initialSource = InitialSource.LOCAL
        )
    )
    PreviewAppTheme {
        PlaylistDetailsScreenContent(
            state = PlaylistDetailsState(
                playlist = Playlist(1, "Rock", 0, 0, 12, 124000L),
                playlistItems = StatedData.Content(items),
                menuItems = persistentListOf(
                    AppMenuItem(1, UiText.DynamicString("Add to queue"), R.drawable.ic_add_to_queue),
                    AppMenuItem(2, UiText.DynamicString("Delete"), R.drawable.ic_playlist_remove),
                )
            ),
            listState = rememberLazyListState(),
            onPlayAllButtonClicked = {},
            onChangeRandomModeClicked = {},
            onTryAgainButtonClicked = {},
            onItemClicked = { _, _ -> },
            onPlaylistEntryMenuItemClicked = { _, _, _ -> },
            onItemMoved = { _, _ -> },
            onItemDragEnded = { _, _ -> },
            onItemSwipedToPlayNext = {},
            onItemSwipedToDelete = {}
        )
    }
}

@Preview
@Composable
private fun PlaylistItemPreview() {
    PreviewAppTheme {
        PlaylistItem(
            item = PlaylistEntry(
                entryId = 1,
                id = 1,
                title = "Composition title",
                artist = "Artist name",
                album = "Album name",
                duration = 123456,
                size = 123456,
                comment = "comment",
                storageId = 1,
                addedTime = 1,
                modifiedTime = 1,
                coverModifyTime = 1,
                fileStatus = LocalFileStatus.AVAILABLE,
                corruptionType = null,
                isFileExists = true,
                initialSource = InitialSource.LOCAL
            ),
            fileSyncState = null,
            isCurrent = false,
            isPlaying = false,
            isCoversEnabled = true,
            menuItems = persistentListOf(),
            isDragging = false,
            onItemClick = {},
            onMenuClick = {}
        )
    }
}

//endregion
