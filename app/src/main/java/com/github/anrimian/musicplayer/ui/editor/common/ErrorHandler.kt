package com.github.anrimian.musicplayer.ui.editor.common

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import com.github.anrimian.musicplayer.ui.common.activity.IntentSenderLauncher
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand

open class ErrorHandler(
    activityResultCaller: ActivityResultCaller,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {

    private val intentSenderLauncher = IntentSenderLauncher(activityResultCaller, { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    })

    fun handleError(errorCommand: ErrorCommand, defaultAction: () -> Unit) {
        if (errorCommand is EditorErrorCommand) {
            intentSenderLauncher.launch(errorCommand.intentSender)
        } else {
            defaultAction()
        }
    }
}