package com.github.anrimian.musicplayer.ui.utils.fragments

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class DialogFragmentDelayRunner(
    private val fragmentManager: FragmentManager,
    private val tag: String?,
    private val delayMillis: Long = 200,
) {

    private val handler = Handler(Looper.getMainLooper())

    fun show(fragment: DialogFragment) {
        handler.postDelayed({
            if (!fragmentManager.isDestroyed) {
                fragment.safeShow(fragmentManager, tag)
            }
        }, delayMillis)
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
        handler.post {
            val fragment = fragmentManager.findFragmentByTag(tag) as DialogFragment?
            fragment?.dismissAllowingStateLoss()
        }
    }
}