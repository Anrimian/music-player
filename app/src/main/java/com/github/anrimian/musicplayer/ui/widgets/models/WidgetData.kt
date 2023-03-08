package com.github.anrimian.musicplayer.ui.widgets.models

class WidgetData(
    var compositionName: String?,
    var compositionAuthor: String?,
    var compositionId: Long,
    var compositionUpdateTime: Long,
    var coverModifyTime: Long,
    var compositionSize: Long,
    var isFileExists: Boolean,
    var queueSize: Int,
    var playerState: Int,
    var randomPlayModeEnabled: Boolean,
    var repeatMode: Int,
    var isCoversEnabled: Boolean,
)