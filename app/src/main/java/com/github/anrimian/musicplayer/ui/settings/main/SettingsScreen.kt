package com.github.anrimian.musicplayer.ui.settings.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.components.AppScaffold
import com.github.anrimian.musicplayer.ui.common.compose.components.snackbar.AppSnackbarHost
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.format.getMissingFilesMessage
import com.github.anrimian.musicplayer.ui.settings.common.SettingsDivider
import com.github.anrimian.musicplayer.ui.settings.common.SettingsItem
import com.github.anrimian.musicplayer.ui.settings.common.SettingsSingleTextItem
import com.github.anrimian.musicplayer.ui.utils.compose.animation.AppAnimatedVerticalVisibility
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseContainer
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.compose.navigationBarPaddingCompat

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
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


    SwipeToCloseContainer(
        modifier = Modifier.navigationBarPaddingCompat(),
        onDismiss = swipeToCloseReporter::onDismiss,
        onDragProgress = swipeToCloseReporter::onDragProgress
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        SettingsScreenContent(
            state = state,
            onMissingCompositionsCountClicked = viewModel::onMissingCompositionsCountClicked,
            onDisplaySettingsClicked = viewModel::onDisplaySettingsClicked,
            onLibrarySettingsClicked = viewModel::onLibrarySettingsClicked,
            onPlayerSettingsClicked = viewModel::onPlayerSettingsClicked,
            onHeadsetSettingsClicked = viewModel::onHeadsetSettingsClicked,
            onThemeSettingsClicked = viewModel::onThemeSettingsClicked,
            onRescanStorageButtonClicked = viewModel::onRescanStorageButtonClicked,
            onRescanStorageButtonLongClick = viewModel::onRescanStorageButtonLongClick,
            snackbarHost = { AppSnackbarHost(snackbarHostState) }
        )
    }
}

@Composable
private fun SettingsScreenContent(
    state: SettingsState,
    onMissingCompositionsCountClicked: () -> Unit,
    onDisplaySettingsClicked: () -> Unit,
    onLibrarySettingsClicked: () -> Unit,
    onPlayerSettingsClicked: () -> Unit,
    onHeadsetSettingsClicked: () -> Unit,
    onThemeSettingsClicked: () -> Unit,
    onRescanStorageButtonClicked: () -> Unit,
    onRescanStorageButtonLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = snackbarHost
    ) { innerPadding ->
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            val missingCompositionsCount = state.missingCompositionsCount
            AppAnimatedVerticalVisibility(visible = missingCompositionsCount > 0) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onMissingCompositionsCountClicked)
                            .padding(
                                horizontal = Dimens.contentHorizontalMargin,
                                vertical = Dimens.contentVerticalMarginSmall
                            )
                    ) {
                        val message = getMissingFilesMessage(missingCompositionsCount)
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = message
                        )
                        Spacer(Modifier.width(Dimens.contentSpacingHorizontalMargin))
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    AppHorizontalDivider()
                }
            }

            SettingsSingleTextItem(
                text = stringResource(R.string.display),
                onClick = onDisplaySettingsClicked
            )
            SettingsDivider()

            SettingsSingleTextItem(
                text = stringResource(R.string.library),
                onClick = onLibrarySettingsClicked
            )
            SettingsDivider()

            SettingsSingleTextItem(
                text = stringResource(R.string.playing),
                onClick = onPlayerSettingsClicked
            )
            SettingsDivider()

            SettingsSingleTextItem(
                text = stringResource(R.string.headset),
                onClick = onHeadsetSettingsClicked
            )
            SettingsDivider()

            SettingsSingleTextItem(
                text = stringResource(R.string.theme),
                onClick = onThemeSettingsClicked
            )
            SettingsDivider()

            SettingsItem(
                title = stringResource(R.string.scan_storage),
                description = stringResource(R.string.scan_storage_description),
                icon = painterResource(R.drawable.ic_storage),
                onClick = onRescanStorageButtonClicked,
                onLongClick = onRescanStorageButtonLongClick
            )

            Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}

//region Previews

@Preview
@Composable
private fun SettingsScreenContentPreview() {
    PreviewAppTheme {
        SettingsScreenContent(
            state = SettingsState(
                missingCompositionsCount = 12
            ),
            onMissingCompositionsCountClicked = {},
            onDisplaySettingsClicked = {},
            onLibrarySettingsClicked = {},
            onPlayerSettingsClicked = {},
            onHeadsetSettingsClicked = {},
            onThemeSettingsClicked = {},
            onRescanStorageButtonClicked = {},
            onRescanStorageButtonLongClick = {}
        )
    }
}

//endregion