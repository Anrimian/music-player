package com.github.anrimian.musicplayer.wear.ui.common

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.wear.activity.ConfirmationActivity
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.github.anrimian.musicplayer.ui.common.onHold
import com.github.anrimian.musicplayer.wear.utils.logger.playTickVibration
import com.google.android.gms.wearable.Wearable

object AppWearUtils {

    fun launchDeviceIntent(context: Context, intent: Intent, message: String?) {
        val remoteActivityHelper = RemoteActivityHelper(context)

        val client = Wearable.getNodeClient(context)
        client.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                Toast.makeText(context, "No connected devices", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            val nodeId = nodes[0].id
            remoteActivityHelper.startRemoteActivity(intent, nodeId)

            val confirmationIntent = Intent(context, ConfirmationActivity::class.java).apply {
                putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION)
                putExtra(ConfirmationActivity.EXTRA_MESSAGE, message)
                putExtra(ConfirmationActivity.EXTRA_ANIMATION_DURATION_MILLIS, 2000)
            }
            context.startActivity(confirmationIntent)
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Unable to open app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun View.onVolumeHold(action: () -> Unit) {
    onHold(
        300,
        80,
        Int.MAX_VALUE,
        { context.playTickVibration() },
        action
    )
}

fun View.onRewindHold(action: () -> Unit) {
    onHold(
        300,
        50,
        15,
        { context.playTickVibration() },
        action
    )
}