package com.github.anrimian.musicplayer.di.utils

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.di.mvvm.compose.ScopedViewModelContainer
import com.github.anrimian.musicplayer.di.mvvm.compose.viewModelWithFactory
import com.github.anrimian.musicplayer.di.mvvm.fragment.viewModelWithFactory

inline fun <reified T : ViewModel> Fragment.viewModel(arg: Parcelable? = null): Lazy<T> {
    return viewModelWithFactory(arg, Components.getAppComponent().viewModelFactory())
}

@Composable
inline fun <reified T : ViewModel> viewModel(
    key: String? = null,
    arg: Parcelable? = null,
): T {
    return viewModelWithFactory(Components.getAppComponent().viewModelFactory(), key, arg)
}

@Composable
inline fun <reified VM : ViewModel> DialogViewModelContainer(
    arg: Parcelable? = null,
    noinline onDismiss: () -> Unit,
    crossinline content: @Composable (viewModel: VM, dismiss: () -> Unit) -> Unit
) {
    ScopedViewModelContainer(
        viewModelFactory = Components.getAppComponent().viewModelFactory(),
        arg = arg,
        onDismiss = onDismiss,
        content = content
    )
}