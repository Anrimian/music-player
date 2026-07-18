package com.github.anrimian.musicplayer.ui.utils.fragments

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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

fun <T> Fragment.observeSideEffects(flow: Flow<T>, onEvent: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { event -> onEvent(event) }
        }
    }
}