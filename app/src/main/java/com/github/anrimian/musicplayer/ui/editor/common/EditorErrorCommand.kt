package com.github.anrimian.musicplayer.ui.editor.common

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.os.Build
import com.github.anrimian.musicplayer.data.storage.providers.music.RecoverableSecurityExceptionExt
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand

@SuppressLint("UseRequiresApi")
@TargetApi(Build.VERSION_CODES.R)
class EditorErrorCommand(message: String, throwable: Throwable) : ErrorCommand(message) {

    val intentSender: IntentSender = when (throwable) {
        is RecoverableSecurityException -> throwable.userAction.actionIntent.intentSender
        is RecoverableSecurityExceptionExt -> throwable.pIntent.intentSender
        else -> throw IllegalStateException("Unknown throwable type: $throwable")
    }

}
