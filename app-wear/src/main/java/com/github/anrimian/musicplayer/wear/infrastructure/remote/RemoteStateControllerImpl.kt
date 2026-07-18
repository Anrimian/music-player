package com.github.anrimian.musicplayer.wear.infrastructure.remote

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.complications.ComplicationDataSourceUpdateRequesterConstants
import androidx.wear.complications.datasource.ComplicationDataSourceUpdateRequester
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.wear.domain.controllers.RemoteStateController
import com.github.anrimian.musicplayer.wear.infrastructure.remote.complications.OpenAppComplication
import com.github.anrimian.musicplayer.wear.infrastructure.remote.complications.PlayPauseComplication

class RemoteStateControllerImpl(
    private val context: Context
): RemoteStateController {

    override fun setIsPlaying(isPlaying: Boolean) {
        //on tile click we'll get unnecessary update
//        TileService.getUpdater(context).requestUpdate(AppTileService::class.java)
        updateComplications()
    }

    override fun setVolumeState(volumeState: VolumeState) {
        //on tile click we'll get unnecessary update
//        TileService.getUpdater(context).requestUpdate(AppTileService::class.java)
//        updateComplications()
    }

    override fun setCurrentComposition(composition: WearableComposition?) {
        updateComplications()
    }

    private fun updateComplications() {
        updateComplication(PlayPauseComplication::class.java)
        updateComplication(OpenAppComplication::class.java)
    }

    @SuppressLint("RestrictedApi")
    private fun updateComplication(complicationClass: Class<*>) {
        val intent = Intent(ComplicationDataSourceUpdateRequester.ACTION_REQUEST_UPDATE_ALL)
        intent.setPackage(ComplicationDataSourceUpdateRequester.UPDATE_REQUEST_RECEIVER_PACKAGE)
        intent.putExtra(
            ComplicationDataSourceUpdateRequester.EXTRA_PROVIDER_COMPONENT,
            ComponentName(context, complicationClass)
        )
        // Add a placeholder PendingIntent to allow the UID to be checked.
        intent.putExtra(
            ComplicationDataSourceUpdateRequesterConstants.EXTRA_PENDING_INTENT,
            //pending intent is in old library broken, there's similar lib in suggestion, try it
            PendingIntent.getActivity(context, 0, Intent(""), PendingIntent.FLAG_IMMUTABLE)//+update current?
        )
        context.sendBroadcast(intent)
//        ComplicationDataSourceUpdateRequester.create(
//            context,
//            ComponentName(context, AppComplicationProviderService::class.java)
//        ).requestUpdateAll()
    }

}