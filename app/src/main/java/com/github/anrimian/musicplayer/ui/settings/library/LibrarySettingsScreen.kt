package com.github.anrimian.musicplayer.ui.settings.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.compose.LocalDeviceCapabilities
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppScaffold
import com.github.anrimian.musicplayer.ui.common.compose.components.ContentTitle
import com.github.anrimian.musicplayer.ui.common.compose.components.LabelledCheckbox
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.EditAllowedExtensionsDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.NumberPickerDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.snackbar.AppSnackbarHost
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.format.AppFormatUtils
import com.github.anrimian.musicplayer.ui.settings.common.SettingsSingleTextItem
import com.github.anrimian.musicplayer.ui.settings.common.SettingsTextItem
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseContainer
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.compose.navigationBarPaddingCompat

@Composable
fun LibrarySettingsScreen(
    viewModel: LibrarySettingsViewModel,
    navigationCallback: (CommonEffect.NavigationEffect) -> Unit,
    actionsCallback: (BaseEffect) -> Unit,
    swipeToCloseReporter: SwipeToCloseReporter
) {
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveEffects(
        flow = viewModel.effects,
        snackbarHostState = snackbarHostState,
        onNavigation = navigationCallback,
        onEffect = actionsCallback
    )

    val dialogs by viewModel.dialogStack.collectAsStateWithLifecycle()
    dialogs.forEach { dialog ->
        when (dialog) {
            is LibrarySettingsDialogs.SelectMinDurationDialog -> {
                SelectMinDurationDialog(
                    currentValue = dialog.currentValue,
                    onDismiss = viewModel::onSelectMinDurationDialogClosed,
                    onConfirm = viewModel::onAudioFileMinDurationMillisPicked
                )
            }
            is LibrarySettingsDialogs.ConfirmMinDurationChangeDialog -> {
                Components.getAppComponent().dialogs().DeleteFilesByImpactConfirmDialog(
                    filesToRemoveCount = dialog.filesToRemoveCount,
                    onDismiss = viewModel::onConfirmDeleteDialogClosed,
                    onConfirm = viewModel::onMinDurationChangeConfirmed
                )
            }
            is LibrarySettingsDialogs.EditAllowedExtensionsDialog -> {
                EditAllowedExtensionsDialog(
                    currentExtensions = dialog.extensions,
                    onDismiss = viewModel::onEditAllowedExtensionsDialogClosed,
                    onConfirm = viewModel::onAllowedExtensionsEditCompleted
                )
            }
            is LibrarySettingsDialogs.ConfirmAllowedExtensionsChangeDialog -> {
                Components.getAppComponent().dialogs().DeleteFilesByImpactConfirmDialog(
                    filesToRemoveCount = dialog.filesToRemoveCount,
                    onDismiss = viewModel::onConfirmDeleteDialogClosed,
                    onConfirm = viewModel::onAllowedExtensionsChangeConfirmed
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
        LibrarySettingsScreenContent(
            state = state,
            onExcludedFoldersClicked = viewModel::onExcludedFoldersClicked,
            onDoNotAppConfirmDialogChecked = viewModel::onDoNotAppConfirmDialogChecked,
            onSelectMinDurationClicked = viewModel::onSelectMinDurationClicked,
            onEditAllowedExtensionsClicked = viewModel::onEditAllowedExtensionsClicked,
            onPlaylistInsertStartChecked = viewModel::onPlaylistInsertStartChecked,
            onPlaylistDuplicateCheckChecked = viewModel::onPlaylistDuplicateCheckChecked,
            snackbarHost = { AppSnackbarHost(snackbarHostState) }
        )
    }
}

@Composable
private fun LibrarySettingsScreenContent(
    state: LibrarySettingsState,
    onExcludedFoldersClicked: () -> Unit,
    onDoNotAppConfirmDialogChecked: (Boolean) -> Unit,
    onSelectMinDurationClicked: () -> Unit,
    onEditAllowedExtensionsClicked: () -> Unit,
    onPlaylistInsertStartChecked: (Boolean) -> Unit,
    onPlaylistDuplicateCheckChecked: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = snackbarHost
    ) { innerPadding ->
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            SettingsSingleTextItem(
                text = stringResource(R.string.excluded_folders),
                onClick = onExcludedFoldersClicked
            )

            if (LocalDeviceCapabilities.current.hasSystemDeleteFileDialog) {
                LabelledCheckbox(
                    label = stringResource(R.string.do_not_show_confirm_delete_dialog),
                    isChecked = state.isDoNotShowAppConfirmDialogEnabled,
                    onCheckedChange = onDoNotAppConfirmDialogChecked
                )
            }

            val seconds = (state.audioFileMinDurationMillis/1000L).toInt()
            SettingsTextItem(
                title = stringResource(R.string.exclude_compositions_with_duration_less_than),
                description = stringResource(
                    R.string.with_duration_less_than,
                    pluralStringResource(R.plurals.seconds_template, seconds, seconds)
                ),
                onClick = onSelectMinDurationClicked
            )

            SettingsTextItem(
                title = stringResource(R.string.allowed_file_extensions),
                description = stringResource(
                    R.string.allowed_extensions_state,
                    AppFormatUtils.formatExtensions(state.allowedFileExtensions)
                ),
                onClick = onEditAllowedExtensionsClicked
            )

            ContentTitle(label = stringResource(R.string.play_lists))

            LabelledCheckbox(
                label = stringResource(R.string.insert_to_playlist_beginning),
                isChecked = state.playlistInsertStartEnabled,
                onCheckedChange = onPlaylistInsertStartChecked
            )

            LabelledCheckbox(
                label = stringResource(R.string.check_for_duplicates),
                isChecked = state.playlistDuplicateCheckEnabled,
                onCheckedChange = onPlaylistDuplicateCheckChecked
            )

            Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}



@Composable
fun SelectMinDurationDialog(
    currentValue: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    NumberPickerDialog(
        minValue = 0,
        maxValue = 120000,
        stepValue = 1000,
        currentValue = currentValue,
        valueFormatter = { millis -> (millis / 1000L).toString() },
        onDismissRequest = onDismiss,
        onValuePicked = onConfirm
    )
}

//region Previews

@Preview
@Composable
private fun LibrarySettingsScreenContentPreview() {
    PreviewAppTheme {
        LibrarySettingsScreenContent(
            state = LibrarySettingsState(
                isDoNotShowAppConfirmDialogEnabled = true,
                audioFileMinDurationMillis = 5000,
                allowedFileExtensions = setOf("mp3", "flac"),
                playlistDuplicateCheckEnabled = true,
                playlistInsertStartEnabled = false
            ),
            onExcludedFoldersClicked = {},
            onDoNotAppConfirmDialogChecked = {},
            onSelectMinDurationClicked = {},
            onEditAllowedExtensionsClicked = {},
            onPlaylistInsertStartChecked = {},
            onPlaylistDuplicateCheckChecked = {}
        )
    }
}

//endregion