package com.github.anrimian.musicplayer.ui.common.compose

import androidx.compose.runtime.staticCompositionLocalOf
import com.github.anrimian.musicplayer.domain.models.common.DeviceCapabilities


val LocalDeviceCapabilities = staticCompositionLocalOf<DeviceCapabilities> {
    error("DeviceCapabilities not provided")
}