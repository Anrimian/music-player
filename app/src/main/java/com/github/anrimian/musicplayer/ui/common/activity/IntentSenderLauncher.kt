package com.github.anrimian.musicplayer.ui.common.activity

import android.content.IntentSender
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

fun Fragment.registerIntentSender(result: (ActivityResult) -> Unit): IntentSenderLauncher {
    return IntentSenderLauncher(this, result) { e ->
        val context = context
        if (context != null) {
            Toast.makeText(
                context,
                "can not start request activity: " + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

class IntentSenderLauncher(
    activityResultCaller: ActivityResultCaller,
    result: (ActivityResult) -> Unit,
    private val onSendError: (IntentSender.SendIntentException) -> Unit = {}
) {

    private val launcher = activityResultCaller.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        result::invoke
    )

    fun launch(intentSender: IntentSender) {
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
            launcher.launch(intentSenderRequest)
        } catch (e: IntentSender.SendIntentException) {
            //would be good to show toast here, but how to pass context here in a nice way?
            onSendError(e)
        }
    }
}