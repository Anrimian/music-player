package com.github.anrimian.musicplayer.di.mvvm.fragment

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.github.anrimian.musicplayer.di.mvvm.MultiBindingViewModelFactory
import com.github.anrimian.musicplayer.di.mvvm.asSavedStateFactory

inline fun <reified T : ViewModel> Fragment.viewModelWithFactory(
    arg: Parcelable? = null,
    viewModelFactory: MultiBindingViewModelFactory
): Lazy<T> {
    return viewModels {
        viewModelFactory.asSavedStateFactory(arg)
    }
}