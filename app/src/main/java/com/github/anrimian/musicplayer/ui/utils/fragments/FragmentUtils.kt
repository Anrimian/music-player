package com.github.anrimian.musicplayer.ui.utils.fragments

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun DialogFragment.safeShow(
    parentFragment: Fragment,
    tag: String? = null
) {
    try {
        //we don't have showAllowingStateLoss, so just consume error
        //https://issuetracker.google.com/issues/37133130
        //Fragment.getChildFragmentManager() also can throw IllegalStateException, consume too
        show(parentFragment.childFragmentManager, tag)
    } catch (ignored: IllegalStateException) {}
}

fun DialogFragment.safeShow(
    fragmentManager: FragmentManager,
    tag: String? = null
) {
    try {
        //we don't have showAllowingStateLoss, so just consume error
        //https://issuetracker.google.com/issues/37133130
        show(fragmentManager, tag)
    } catch (ignored: IllegalStateException) {}
}