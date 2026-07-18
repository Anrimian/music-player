package com.github.anrimian.musicplayer.di.mvvm.compose

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.anrimian.musicplayer.di.mvvm.MultiBindingViewModelFactory
import com.github.anrimian.musicplayer.di.mvvm.asSavedStateFactory

@Composable
inline fun <reified T : ViewModel> viewModelWithFactory(
    viewModelFactory: MultiBindingViewModelFactory,
    key: String? = null,
    arg: Parcelable? = null,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
    extras: CreationExtras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }
): T {
    return viewModel(
        key = key,
        viewModelStoreOwner = viewModelStoreOwner,
        extras = extras,
        factory = viewModelFactory.asSavedStateFactory(arg)
    )
}