package com.github.anrimian.musicplayer.ui.utils.fragments

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.lang.IllegalStateException

fun DialogFragment.safeShow(
    fragmentManager: FragmentManager,
    tag: String? = null
) {
    try {
        //we don't have showAllowingStateLoss, so just consume error
        show(fragmentManager, tag)
    } catch (ignored: IllegalStateException) {}
}