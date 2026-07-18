package com.github.anrimian.musicplayer.ui.settings.folders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.components.AppScaffold
import com.github.anrimian.musicplayer.ui.common.compose.components.progress.ProgressState
import com.github.anrimian.musicplayer.ui.common.compose.components.snackbar.AppSnackbarHost
import com.github.anrimian.musicplayer.ui.common.compose.medium
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.utils.compose.appItemAnimation
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseContainer
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.compose.navigationBarPaddingCompat
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ExcludedFoldersScreen(
    viewModel: ExcludedFoldersViewModel,
    swipeToCloseReporter: SwipeToCloseReporter
) {

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveEffects(
        flow = viewModel.effects,
        snackbarHostState = snackbarHostState,
        onMessageAction = { action ->
            when (action) {
                RestoreRemovedFolder -> viewModel.onRestoreRemovedFolderClicked()
            }
        }
    )

    SwipeToCloseContainer(
        modifier = Modifier.navigationBarPaddingCompat(),
        onDismiss = swipeToCloseReporter::onDismiss,
        onDragProgress = swipeToCloseReporter::onDragProgress
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        ExcludedFoldersScreenContent(
            state = state,
            onDeleteFolderClicked = viewModel::onDeleteFolderClicked,
            snackbarHost = { AppSnackbarHost(snackbarHostState) }
        )
    }
}

@Composable
private fun ExcludedFoldersScreenContent(
    state: ExcludedFoldersState,
    onDeleteFolderClicked: (IgnoredFolder) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = snackbarHost
    ) { innerPadding ->
        ProgressState(
            state = state.folders,
            contentPadding = innerPadding
        ) { folders ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                items(
                    items = folders,
                    key = { folder -> folder.path },
                    contentType = { "ignored_folder" }
                ) { folder ->
                    val onRemoveClick = remember(folder) {
                        { onDeleteFolderClicked(folder) }
                    }
                    ExcludedFolderItem(
                        modifier = Modifier.appItemAnimation(this),
                        folder = folder,
                        onRemoveClick = onRemoveClick
                    )
                }
            }
        }
    }
}


@Composable
private fun ExcludedFolderItem(
    folder: IgnoredFolder,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.contentHorizontalMargin,
                    top = Dimens.contentInternalVerticalMargin,
                    bottom = Dimens.contentInternalVerticalMargin,
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = folder.path,
                style = MaterialTheme.typography.medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onRemoveClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.remove_excluded_folder),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        AppHorizontalDivider()
    }
}

//region Previews

@Preview
@Composable
private fun ExcludedFoldersScreenContentPreview() {
    val folders = persistentListOf(
        IgnoredFolder("/storage/emulated/0/Music/Excluded", 0L),
        IgnoredFolder("/storage/emulated/0/Android/data", 0L),
        IgnoredFolder("/storage/emulated/0/DCIM/.thumbnails", 0L),
    )
    PreviewAppTheme {
        ExcludedFoldersScreenContent(
            state = ExcludedFoldersState(
                folders = StatedData.Content(folders)
            ),
            onDeleteFolderClicked = {}
        )
    }
}

//endregion