package com.github.anrimian.musicplayer.di.mvvm

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.github.anrimian.musicplayer.ui.common.mvvm.BaseViewModel.Companion.VM_ARG_KEY

/**
 * Creates a generic ViewModelProvider.Factory that injects arguments into SavedStateHandle
 * and delegates creation to MultiBindingViewModelFactory.
 */
fun MultiBindingViewModelFactory.asSavedStateFactory(
    arg: Parcelable?
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val newExtras = if (arg != null) {
            val argsBundle = Bundle().apply {
                putParcelable(VM_ARG_KEY, arg)
            }
            MutableCreationExtras(extras).apply {
                set(DEFAULT_ARGS_KEY, argsBundle)
            }
        } else {
            extras
        }

        val handle = newExtras.createSavedStateHandle()
        @Suppress("USELESS_CAST")
        return this@asSavedStateFactory.create(modelClass, handle) as T
    }
}