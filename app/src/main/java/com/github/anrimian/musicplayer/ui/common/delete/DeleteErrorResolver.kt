package com.github.anrimian.musicplayer.ui.common.delete

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberDeleteErrorResolver(
    onPermissionResult: (Boolean) -> Unit
): (ShowDeleteErrorEffect) -> Unit {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        onPermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    return remember(launcher) {
        { effect ->
            val request = IntentSenderRequest.Builder(effect.errorCommand.intentSender).build()
            launcher.launch(request)
        }
    }
}