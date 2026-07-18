package com.github.anrimian.musicplayer.ui.settings.headset

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.hasBluetoothConnectPermission
import com.github.anrimian.musicplayer.infrastructure.receivers.BluetoothConnectionReceiver
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppScaffold
import com.github.anrimian.musicplayer.ui.common.compose.components.ContentTitle
import com.github.anrimian.musicplayer.ui.common.compose.components.LabelledCheckbox
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.NumberPickerDialog
import com.github.anrimian.musicplayer.ui.common.compose.components.snackbar.AppSnackbarHost
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.utils.compose.components.buttons.ImageButton
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseContainer
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.compose.navigationBarPaddingCompat
import com.github.anrimian.musicplayer.ui.utils.startAppSettings

@Composable
fun HeadsetSettingsScreen(
    viewModel: HeadsetSettingsViewModel,
    navigationCallback: (CommonEffect.NavigationEffect) -> Unit,
    swipeToCloseReporter: SwipeToCloseReporter,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val shouldShowRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            false
        }
        viewModel.onBluetoothConnectPermissionResult(granted, shouldShowRationale)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!hasBluetoothConnectPermission(context)) {
                    viewModel.onBluetoothConnectPermissionRevoked()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ObserveEffects(
        flow = viewModel.effects,
        snackbarHostState = snackbarHostState,
        onMessageAction = { action ->
            when (action) {
                OpenAppSettings -> startAppSettings(context as Activity)
            }
        },
        onNavigation = navigationCallback,
        onEffect = { effect ->
            when (effect) {
                is HeadsetSettingsEffect.RequestBluetoothConnectPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                }
                is HeadsetSettingsEffect.SetBluetoothReceiverEnabled -> {
                    BluetoothConnectionReceiver.setEnabled(context, effect.isEnabled)
                }
            }
        }
    )

    val dialogs by viewModel.dialogStack.collectAsStateWithLifecycle()
    dialogs.forEach { dialog ->
        when (dialog) {
            is HeadsetSettingsDialogs.PlayDelayPickerDialog -> {
                PlayDelayPickedDialog(
                    currentValue = dialog.currentValue,
                    onDismissRequest = viewModel::onConnectAutoPlayDelayDismissed,
                    onValuePicked = viewModel::onConnectAutoPlayDelaySelected
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

        HeadsetSettingsScreenContent(
            state = state,
            onPlayOnConnectChecked = { checked ->
                viewModel.onPlayOnConnectChecked(checked, hasBluetoothConnectPermission(context))
            },
            onPickPlayDelayClicked = viewModel::onPickPlayDelayClicked,
            onIgnorePlayAfterConnectionChecked = { checked ->
                viewModel.onIgnorePlayAfterConnectionChecked(checked, hasBluetoothConnectPermission(context))
            },
            onProcessUnsupportedEventsChecked = viewModel::onProcessUnsupportedEventsChecked,
            snackbarHost = { AppSnackbarHost(snackbarHostState) }
        )
    }
}

@Composable
private fun HeadsetSettingsScreenContent(
    state: HeadsetSettingsState,
    onPlayOnConnectChecked: (Boolean) -> Unit,
    onPickPlayDelayClicked: () -> Unit,
    onIgnorePlayAfterConnectionChecked: (Boolean) -> Unit,
    onProcessUnsupportedEventsChecked: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = snackbarHost
    ) { innerPadding ->
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                val description = stringResource(R.string.with_delay, state.bluetoothConnectAutoPlayDelay / 1000f)
                LabelledCheckbox(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.play_on_connect_bluetooth_device),
                    description = description,
                    isChecked = state.isBluetoothAutoPlayEnabled,
                    onCheckedChange = onPlayOnConnectChecked
                )

                ImageButton(
                    iconRes = R.drawable.ic_timer,
                    contentDescription = description,
                    onClick = onPickPlayDelayClicked
                )

                Spacer(Modifier.width(Dimens.contentHorizontalMargin))
            }
            ContentTitle(label = stringResource(R.string.advanced_settings))

            LabelledCheckbox(
                label = stringResource(R.string.ignore_play_after_connection),
                description = stringResource(R.string.ignore_play_after_connection_description),
                isChecked = state.isIgnorePlayAfterConnectionEnabled,
                onCheckedChange = onIgnorePlayAfterConnectionChecked
            )

            LabelledCheckbox(
                label = stringResource(R.string.process_unsupported_actions),
                description = stringResource(R.string.process_unsupported_actions_description),
                isChecked = state.isProcessUnsupportedBluetoothEventEnabled,
                onCheckedChange = onProcessUnsupportedEventsChecked
            )

            Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}

@Composable
fun PlayDelayPickedDialog(
    currentValue: Long,
    onDismissRequest: () -> Unit,
    onValuePicked: (Long) -> Unit
) {
    NumberPickerDialog(
        minValue = 0,
        maxValue = 5000,
        stepValue = 100,
        currentValue = currentValue,
        valueFormatter = { millis -> (millis / 1000f).toString() },
        onDismissRequest = onDismissRequest,
        onValuePicked = onValuePicked
    )
}

//region Previews

@Preview
@Composable
private fun HeadsetSettingsScreenContentPreview() {
    PreviewAppTheme {
        HeadsetSettingsScreenContent(
            state = HeadsetSettingsState(
                bluetoothConnectAutoPlayDelay = 1000L,
                isProcessUnsupportedBluetoothEventEnabled = true,
                isIgnorePlayAfterConnectionEnabled = false,
                isBluetoothAutoPlayEnabled = true
            ),
            onPlayOnConnectChecked = {},
            onPickPlayDelayClicked = {},
            onIgnorePlayAfterConnectionChecked = {},
            onProcessUnsupportedEventsChecked = {}
        )
    }
}

//endregion

