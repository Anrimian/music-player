package com.github.anrimian.musicplayer.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils
import java.io.File

object ExternalAppIntents {

    fun File.startViewTextFileScreen(activity: Activity) {
        val uri = AppAndroidUtils.createUri(activity, this)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "text/*")
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, "Text view app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun File.startEmailSendFileScreen(
        activity: Activity,
        subject: String,
        email: String,
        chooserTitle: String
    ) {
        val uri = AppAndroidUtils.createUri(activity, this)

        val emailIntent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
        val emailAppActivities = activity.packageManager.queryIntentActivities(emailIntent, 0)
        if (emailAppActivities.isEmpty()) {
            Toast.makeText(activity, "Mail app not found", Toast.LENGTH_SHORT).show()
            return
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val targetedIntents = emailAppActivities.map { resInfo ->
            val targeted = Intent(sendIntent)
            targeted.setPackage(resInfo.activityInfo.packageName)
            targeted
        }.toTypedArray()

        val chooserIntent = Intent.createChooser(Intent(), chooserTitle)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents)

        try {
            activity.startActivity(chooserIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, "Mail app not found", Toast.LENGTH_SHORT).show()
        }
    }

}
