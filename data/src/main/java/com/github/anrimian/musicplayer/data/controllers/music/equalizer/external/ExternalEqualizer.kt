package com.github.anrimian.musicplayer.data.controllers.music.equalizer.external

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer

class ExternalEqualizer(private val context: Context) : AppEqualizer {

    override fun attachEqualizer(audioSessionId: Int) {
        val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        context.sendBroadcast(intent)
    }

    override fun detachEqualizer(audioSessionId: Int) {
        val intent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        context.sendBroadcast(intent)
    }

    fun launchExternalEqualizerSetup(activity: Activity, audioSessionId: Int) {
        if (audioSessionId == AudioEffect.ERROR_BAD_VALUE) {
            Toast.makeText(activity, "No Session Id", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, activity.packageName)
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            activity.startActivityForResult(intent, 0)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "Unable to start eq: " + e.message, Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Toast.makeText(activity, "Unable to start eq: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {

        fun isExternalEqualizerExists(context: Context): Boolean {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            return intent.resolveActivity(context.packageManager) != null
        }

    }
}