package com.github.anrimian.musicplayer.ui.playlists.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppFastScroller
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.components.AppScaffold
import com.github.anrimian.musicplayer.ui.common.compose.components.PopupMenuButton
import com.github.anrimian.musicplayer.ui.common.compose.components.TextWithSeparators
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.ConfirmDeletePlaylistDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.ConfirmDeletePlaylistDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.MenuConfigDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.MenuConfigDialogData
import com.github.anrimian.musicplayer.ui.common.compose.components.fab.AppFloatingActionButton
import com.github.anrimian.musicplayer.ui.common.compose.components.fab.rememberScrollingFabVisibility
import com.github.anrimian.musicplayer.ui.common.compose.components.progress.ProgressState
import com.github.anrimian.musicplayer.ui.common.compose.components.snackbar.AppSnackbarHost
import com.github.anrimian.musicplayer.ui.common.compose.contentSubtitle
import com.github.anrimian.musicplayer.ui.common.compose.itemPrimary
import com.github.anrimian.musicplayer.ui.common.compose.selectionContainer
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.format.AppTimeFormatUtils
import com.github.anrimian.musicplayer.ui.common.lists.ScrollToPositionEffect
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.common.models.menu.MenuIds
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.library.common.library.LibraryDialogHost
import com.github.anrimian.musicplayer.ui.playlists.create.CreatePlaylistDialog
import com.github.anrimian.musicplayer.ui.playlists.create.CreatePlaylistDialogData
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlaylistDialog
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlaylistDialogData
import com.github.anrimian.musicplayer.ui.utils.compose.SearchScrollHandler
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import com.github.anrimian.musicplayer.ui.utils.compose.appItemAnimation
import com.github.anrimian.musicplayer.ui.utils.compose.attachStopCallback
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.AppAlertDialog
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SingleActionSwipeContainer
import com.github.anrimian.musicplayer.ui.utils.compose.scrollToPosition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel,
    navigationCallback: (CommonEffect.NavigationEffect) -> Unit,
    actionsCallback: (PlaylistsEffect) -> Unit,
    selectionModeCallback: (SelectionModeState?) -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    listState.attachStopCallback(viewModel::onStop)

    ObserveEffects(
        flow = viewModel.effects,
        snackbarHostState = snackbarHostState,
        onNavigation = navigationCallback,
        onEffect = { effect ->
            when (effect) {
                is ScrollToPositionEffect -> {
                    listState.scrollToPosition(scope, effect.position)
                }
                is PlaylistsEffect -> actionsCallback(effect)
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
            is PlaylistsDialogs.OverwritePlaylistDialog -> {
                OverwritePlaylistDialog(
                    onDismiss = viewModel::onOverwritePlaylistDialogClosed,
                    onConfirm = viewModel::onOverwritePlaylistDialogConfirmed
                )
            }
            is PlaylistsDialogs.NotCompletelyImportedPlaylistDialog -> {
                NotCompletelyImportedPlaylistDialog(
                    data = dialog,
                    onDismiss = viewModel::onNotCompletelyImportedPlaylistDialogClosed,
                    onConfirm = viewModel::onNotCompletelyImportedPlaylistDialogConfirmed
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
                    onConfirm = viewModel::onConfirmDeletePlaylistsDialogConfirmed,
                    onDismiss = viewModel::onConfirmDeletePlaylistsDialogClosed
                )
            }
            is CreatePlaylistDialogData -> {
                CreatePlaylistDialog(
                    onDismiss = viewModel::onCreatePlaylistDialogClosed
                )
            }
            else -> {}
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.selectionModeState) {
        selectionModeCallback(state.selectionModeState)
    }

    PlaylistsScreenContent(
        state = state,
        listState = listState,
        onPlayNextPlaylistClicked = viewModel::onPlayNextPlaylistClicked,
        onPlaylistClicked = viewModel::onPlaylistClicked,
        onPlaylistLongClicked = viewModel::onPlaylistLongClicked,
        onPlaylistMenuItemClicked = viewModel::onPlaylistMenuItemClicked,
        onCreatePlaylistButtonClicked = viewModel::onCreatePlaylistButtonClicked,
        onTryAgainButtonClicked = viewModel::onTryAgainButtonClicked,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    )
}

@Composable
private fun PlaylistsScreenContent(
    state: PlaylistsState,
    listState: LazyListState,
    onPlayNextPlaylistClicked: (Playlist) -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
    onPlaylistLongClicked: (Playlist) -> Unit,
    onPlaylistMenuItemClicked: (AppMenuItem, Playlist) -> Unit,
    onCreatePlaylistButtonClicked: () -> Unit,
    onTryAgainButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    val (isFabVisible, fabNestedScrollConnection) = rememberScrollingFabVisibility(listState)

    AppScaffold(
        modifier = modifier.fillMaxSize().nestedScroll(fabNestedScrollConnection),
        snackbarHost = snackbarHost,
        floatingActionButton = {
            AppFloatingActionButton(
                onClick = onCreatePlaylistButtonClicked,
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = stringResource(R.string.create_playlist),
                visible = isFabVisible.value,
            )
        },
    ) { innerPadding ->
        SearchScrollHandler(
            listState = listState,
            searchQuery = state.searchQuery,
            dataState = state.playlists,
            isContentReady = { dataState -> dataState is StatedData.Content },
            getItems = { dataState -> dataState.data }
        )

        ProgressState(
            state = state.playlists,
            contentPadding = innerPadding,
            errorAction = onTryAgainButtonClicked
        ) { playlists ->
            val bottomPadding = innerPadding.calculateBottomPadding() + Dimens.bottomPaddingWithFab

            Box(modifier = Modifier.fillMaxSize()) {

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = bottomPadding
                    )
                ) {
                    items(
                        items = playlists,
                        key = { playList -> playList.id },
                        contentType = { "Playlist" }
                    ) { playList ->
                        val isSelected = state.selectedPlaylists.contains(playList.id)

                        PlaylistsScreenItem(
                            playList = playList,
                            isSelected = isSelected,
                            menuItems = state.menuItems,
                            onPlayNextPlaylistClicked = onPlayNextPlaylistClicked,
                            onPlaylistClicked = onPlaylistClicked,
                            onPlaylistLongClicked = onPlaylistLongClicked,
                            onPlaylistMenuItemClicked = onPlaylistMenuItemClicked
                        )
                    }
                }

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
private fun LazyItemScope.PlaylistsScreenItem(
    playList: Playlist,
    isSelected: Boolean,
    menuItems: ImmutableList<AppMenuItem>,
    onPlayNextPlaylistClicked: (Playlist) -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
    onPlaylistLongClicked: (Playlist) -> Unit,
    onPlaylistMenuItemClicked: (AppMenuItem, Playlist) -> Unit,
) {
    Column(
        modifier = Modifier.appItemAnimation(this)
    ) {
        SingleActionSwipeContainer(
            onAction = { onPlayNextPlaylistClicked(playList) },
            icon = painterResource(R.drawable.ic_play_next),
            text = stringResource(R.string.play_next),
            enabled = playList.compositionsCount > 0,
        ) {
            PlaylistItem(
                playList = playList,
                menuItems = menuItems,
                selected = isSelected,
                onItemClick = onPlaylistClicked,
                onLongClick = onPlaylistLongClicked,
                onMenuClick = onPlaylistMenuItemClicked
            )
        }

        AppHorizontalDivider()
    }
}

@Composable
fun PlaylistItem(
    playList: Playlist,
    menuItems: ImmutableList<AppMenuItem>,
    selected: Boolean,
    onItemClick: (Playlist) -> Unit,
    onLongClick: (Playlist) -> Unit,
    onMenuClick: (AppMenuItem, Playlist) -> Unit,
    modifier: Modifier = Modifier,
) {

    val selectionOverlayColorState = animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.selectionContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "selectionAnimation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind { drawRect(selectionOverlayColorState.value) }
            .combinedClickable(
                onClick = { onItemClick(playList) },
                onLongClick = { onLongClick(playList) }
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    horizontal = Dimens.contentHorizontalMargin,
                    vertical = Dimens.listVerticalMargin
                )
        ) {
            Text(
                text = playList.name,
                style = MaterialTheme.typography.itemPrimary,
            )

            Spacer(modifier = Modifier.height(Dimens.contentSpacingVerticalMargin))

            PlaylistAdditionalInfo(
                compositionsCount = playList.compositionsCount,
                totalDuration = playList.totalDuration
            )
        }

        val filteredMenuItems = remember(menuItems, playList.compositionsCount) {
            menuItems.filter { item ->
                isItemVisible(item.id, playList.compositionsCount)
            }.toImmutableList()
        }

        PopupMenuButton(
            menuItems = filteredMenuItems,
            onItemClick = { menuItem -> onMenuClick(menuItem, playList) }
        )
    }
}

@Composable
fun PlaylistAdditionalInfo(
    compositionsCount: Int,
    totalDuration: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.contentSubtitle,
) {
    val countString = pluralStringResource(R.plurals.compositions_count, compositionsCount, compositionsCount)
    val items = remember(countString, totalDuration) {
        val countText = AnnotatedString(countString)
        val durationText = AnnotatedString(AppTimeFormatUtils.formatMilliseconds(totalDuration))
        persistentListOf(countText, durationText)
    }

    TextWithSeparators(
        items = items,
        modifier = modifier,
        style = style
    )
}

private fun isItemVisible(menuId: Int, compositionsCount: Int): Boolean {
    if (compositionsCount > 0) {
        return true
    }
    return when (menuId) {
        MenuIds.PLAY,
        MenuIds.PLAY_NEXT,
        MenuIds.ADD_TO_QUEUE,
        MenuIds.ADD_TO_PLAYLIST,
        MenuIds.SHARE -> false
        else -> true
    }
}

@Composable
private fun OverwritePlaylistDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AppAlertDialog(
        message = stringResource(R.string.overwrite_playlist),
        onDismissRequest = onDismiss,
        positiveText = stringResource(android.R.string.ok),
        positiveAction = { onConfirm() },
        negativeText = stringResource(android.R.string.cancel)
    )
}

@Composable
private fun NotCompletelyImportedPlaylistDialog(
    data: PlaylistsDialogs.NotCompletelyImportedPlaylistDialog,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val message = pluralStringResource(
        R.plurals.playlist_import_partial_success,
        data.notFoundFilesCount,
        data.notFoundFilesCount
    )
    AppAlertDialog(
        message = message,
        onDismissRequest = onDismiss,
        positiveText = stringResource(android.R.string.ok),
        positiveAction = { onConfirm() },
    )
}

//region Previews

@Preview
@Composable
private fun PlaylistsScreenContentPreview() {
    val playlists = persistentListOf(
        Playlist(1, "Favorites", 0L, 0L, 12, 124000L),
        Playlist(2, "Rock", 0L, 0L, 45, 1240000L),
        Playlist(3, "Jazz", 0L, 0L, 0, 0L),
    )
    PreviewAppTheme {
        PlaylistsScreenContent(
            state = PlaylistsState(
                playlists = StatedData.Content(playlists),
                menuItems = persistentListOf(
                    AppMenuItem(MenuIds.PLAY, UiText.StringResource(R.string.play)),
                    AppMenuItem(MenuIds.EDIT_NAME, UiText.StringResource(R.string.edit)),
                    AppMenuItem(MenuIds.DELETE, UiText.StringResource(R.string.delete)),
                )
            ),
            listState = rememberLazyListState(),
            onPlayNextPlaylistClicked = {},
            onPlaylistClicked = {},
            onPlaylistLongClicked = {},
            onPlaylistMenuItemClicked = { _, _ -> },
            onCreatePlaylistButtonClicked = {},
            onTryAgainButtonClicked = {},
        )
    }
}

//endregion
