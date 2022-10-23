package com.github.anrimian.musicplayer.ui.utils.fragments

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.util.*

class DialogFragmentDelayRunner<T: DialogFragment>(
    private val fragmentManager: FragmentManager,
    private val tag: String?,
    private val fragmentInitializer: ((T) -> Unit)? = null,
    private val delayMillis: Long = 200,
) {

    private val handler = Handler(Looper.getMainLooper())

    private val deferredActions = LinkedList<(T) -> Unit>()

    init {
        fragmentManager.findFragmentByTag(tag)?.apply {
            @Suppress("UNCHECKED_CAST")
            fragmentInitializer?.invoke(this as T)
        }
    }

    fun show(fragment: T) {
        handler.postDelayed({
            if (!fragmentManager.isDestroyed && getFragment() == null) {
                fragmentInitializer?.invoke(fragment)
                fragment.safeShow(fragmentManager, tag)

                handler.post {
                    while (!deferredActions.isEmpty()) {
                        deferredActions.pollFirst()!!.invoke(fragment)
                    }
                }
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

    @Suppress("UNCHECKED_CAST")
    fun getFragment(): T? = fragmentManager.findFragmentByTag(tag) as T?

    fun runAction(action: (T) -> Unit) {
        val fragment = getFragment()
        if (fragment != null) {
            action(fragment)
        } else {
            deferredActions.add(action)
        }
    }
}