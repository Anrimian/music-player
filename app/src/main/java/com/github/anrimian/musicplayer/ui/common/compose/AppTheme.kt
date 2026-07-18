package com.github.anrimian.musicplayer.ui.common.compose

import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.common.DeviceCapabilities
import com.github.anrimian.musicplayer.ui.common.compose.text.LocalUiTextResolver
import com.github.anrimian.musicplayer.ui.common.compose.text.UiTextResolver
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme
import kotlinx.coroutines.rx3.asFlow

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val appComponent = Components.getAppComponent()
    val themeController = appComponent.themeController()

    val theme by themeController.getAppThemeObservable().asFlow()
        .collectAsState(themeController.getCurrentTheme())

    val isCircleShape by themeController.getRoundCoversObservable().asFlow()
        .collectAsState(themeController.isCircleShapeEnabled())

    val capabilities = appComponent.deviceCapabilities()

    BaseAppTheme(
        theme = theme,
        isCircleShape = isCircleShape,
        capabilities = capabilities,
        content = content
    )
}

@SuppressLint("NewApi")
@Composable
fun PreviewAppTheme(
    isDark: Boolean = !isSystemInDarkTheme(),
    isCircleShape: Boolean = false,
    capabilities: DeviceCapabilities = DeviceCapabilities(
        hasSystemDeleteFileDialog = true,
        isResizeablePopupsSupported = true,
        isClipboardVisualConfirmationSupported = true,
        isHardwareAcceleratedClippingSupported = true
    ),
    content: @Composable () -> Unit
) {
    val mockTheme = if (isDark) AppTheme.getSystemDarkTheme() else AppTheme.getSystemWhiteTheme()

    BaseAppTheme(
        theme = mockTheme,
        isCircleShape = isCircleShape,
        capabilities = capabilities,
        content = content
    )
}

@Composable
fun BaseAppTheme(
    theme: AppTheme,
    isCircleShape: Boolean,
    capabilities: DeviceCapabilities,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val colorScheme = remember(baseContext, theme) {
        val themeContext = ContextThemeWrapper(baseContext, theme.themeResId)
        createColorScheme(themeContext, theme.isDark)
    }

    val appShapes = remember(isCircleShape) { calculateAppShapes(isCircleShape) }

    val dimens = calculateAppDimens()

    val rippleConfig = calculateRippleConfiguration(theme.isDark)

    CompositionLocalProvider(
        LocalAppDimens provides dimens,
        LocalAdditionalColors provides AppAdditionalColors(),
        LocalUiTextResolver provides UiTextResolver(baseContext),
        LocalDeviceCapabilities provides capabilities,
        LocalRippleConfiguration provides rippleConfig,
        LocalAppShapes provides appShapes
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OldSchoolTypography,
            content = content
        )
    }
}




