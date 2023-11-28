package com.github.anrimian.musicplayer.domain.models.player.service

data class MusicNotificationSetting(
    val isShowCovers: Boolean,
    val isColoredNotification: Boolean,
    val isShowNotificationCoverStub: Boolean,
    val isCoversOnLockScreen: Boolean
)