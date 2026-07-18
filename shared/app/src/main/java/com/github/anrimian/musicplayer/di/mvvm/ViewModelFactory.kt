package com.github.anrimian.musicplayer.di.mvvm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import javax.inject.Provider

class MultiBindingViewModelFactory(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModelAssistedFactory<*>>>
) {
    fun <T : ViewModel> create(modelClass: Class<T>, handle: SavedStateHandle): T {
        val creator = creators[modelClass] ?: throw IllegalArgumentException("Unknown $modelClass")
        @Suppress("UNCHECKED_CAST")
        return creator.get().create(handle) as T
    }
}

fun interface ViewModelAssistedFactory<T : ViewModel> {
    fun create(handle: SavedStateHandle): T
}