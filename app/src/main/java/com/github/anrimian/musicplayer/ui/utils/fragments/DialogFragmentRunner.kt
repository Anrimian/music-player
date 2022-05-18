package com.github.anrimian.musicplayer.ui.utils.fragments

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class DialogFragmentRunner<T : DialogFragment>(
    private val fragmentManager: FragmentManager,
    private val tag: String,
    private val fragmentInitializer: (T) -> Unit
) {

    init {
        fragmentManager.findFragmentByTag(tag)?.apply {
            @Suppress("UNCHECKED_CAST")
            fragmentInitializer(this as T)
        }
    }

    fun show(fragment: T) {
        fragmentInitializer(fragment)
        fragment.safeShow(fragmentManager, tag)
    }


}