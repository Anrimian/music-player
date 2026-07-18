package com.github.anrimian.musicplayer.domain.models.common

data class DeviceCapabilities(
    val hasSystemDeleteFileDialog: Boolean,
    val isResizeablePopupsSupported: Boolean,
    val isClipboardVisualConfirmationSupported: Boolean,
    val isHardwareAcceleratedClippingSupported: Boolean
)