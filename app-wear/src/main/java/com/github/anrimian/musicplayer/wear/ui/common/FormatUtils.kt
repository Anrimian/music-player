package com.github.anrimian.musicplayer.wear.ui.common

import android.content.Context
import androidx.annotation.StringRes
import com.github.anrimian.common.WearableEvents
import com.github.anrimian.common.WearableFields
import com.github.anrimian.domain.models.ExternalWearableComposition
import com.github.anrimian.domain.models.LibraryWearableComposition
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.wear.R
import com.github.anrimian.musicplayer.wear.domain.models.DeviceState
import com.github.anrimian.musicplayer.wear.domain.models.ErrorEvent

object FormatUtils {

    fun formatErrorEvent(context: Context, errorEvent: ErrorEvent): String {
        val shortEventName = when(errorEvent.eventName) {
            WearableEvents.PLAY_PAUSE -> "pp"
            WearableEvents.SKIP_TO_NEXT -> "stn"
            WearableEvents.SKIP_TO_PREVIOUS -> "stp"
            WearableEvents.SEEK_TO -> "st"
            WearableEvents.FAST_SEEK_FORWARD -> "fsf"
            WearableEvents.FAST_SEEK_BACKWARD -> "fsb"
            WearableEvents.SKIP_TO_ITEM -> "pp"
            WearableEvents.REQUEST_APP_STATE -> "ras"
            else -> errorEvent.eventName
        }
        val shortErrorType = when(errorEvent.errorType) {
            WearableFields.ERROR_NO_PERMISSION -> "no_per"
            WearableFields.ERROR_NO_ACK_EVENT -> "no_ack"
            WearableFields.ERROR_SEND_EVENT -> "unsent"
            else -> errorEvent.errorType
        }
        val msg = errorEvent.throwable?.message
        return context.getString(R.string.state_error_template, shortEventName, shortErrorType, msg)
    }

    @StringRes
    fun formatDeviceStateError(deviceState: DeviceState) = when(deviceState) {
        DeviceState.NOT_CONNECTED -> R.string.host_app_not_found
        DeviceState.HOST_UPDATE_REQUIRED -> R.string.update_host_app
        DeviceState.WEAR_UPDATE_REQUIRED -> R.string.update_wear_app
        else -> throw IllegalStateException()
    }

    fun formatCompositionTitle(context: Context, composition: WearableComposition?): String {
        return when(composition) {
            is LibraryWearableComposition -> composition.title
            is ExternalWearableComposition -> composition.title
            null -> "no current composition"
        }
    }

    fun formatCompositionArtist(context: Context, composition: WearableComposition?): String {
        return when(composition) {
            is LibraryWearableComposition -> composition.artist
            is ExternalWearableComposition -> composition.artist
            null -> ""
        } ?: "unknown artist"
    }

}