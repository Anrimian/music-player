package com.github.anrimian.musicplayer.di.mvvm.compose

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.anrimian.musicplayer.di.mvvm.MultiBindingViewModelFactory
import java.util.UUID

/**
 * A wrapper that provides a scoped ViewModelStore for a specific dialog session.
 * It ensures the ViewModel survives rotation but is cleared when the dialog is dismissed.
 */
@Composable
inline fun <reified VM : ViewModel> ScopedViewModelContainer(
    viewModelFactory: MultiBindingViewModelFactory,
    arg: Parcelable? = null,
    noinline onDismiss: () -> Unit,
    crossinline content: @Composable (viewModel: VM, dismiss: () -> Unit) -> Unit
) {
    val holder: ViewModelScopeHolder = viewModel()

    val sessionKey = rememberSaveable { UUID.randomUUID().toString() }

    val viewModel = getScopedViewModel<VM>(
        key = sessionKey,
        arg = arg,
        holder = holder,
        viewModelFactory = viewModelFactory
    )

    val wrappedDismiss = remember(holder, sessionKey, onDismiss) {
        {
            holder.clearStore(sessionKey)
            onDismiss()
        }
    }

    content(viewModel, wrappedDismiss)
}

@PublishedApi
@Composable
internal inline fun <reified T : ViewModel> getScopedViewModel(
    key: String,
    arg: Parcelable?,
    holder: ViewModelScopeHolder,
    viewModelFactory: MultiBindingViewModelFactory
): T {
    val dialogStore = holder.getStore(key)

    val owner = remember(dialogStore) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = dialogStore
        }
    }

    val currentOwner = LocalViewModelStoreOwner.current
    val defaultExtras = if (currentOwner is HasDefaultViewModelProviderFactory) {
        currentOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }

    return viewModelWithFactory(
        key = key,
        arg = arg,
        viewModelStoreOwner = owner,
        extras = defaultExtras,
        viewModelFactory = viewModelFactory
    )
}

@PublishedApi
internal class ViewModelScopeHolder : ViewModel() {
    private val stores = mutableMapOf<String, ViewModelStore>()

    fun getStore(key: String): ViewModelStore {
        return stores.getOrPut(key) { ViewModelStore() }
    }

    fun clearStore(key: String) {
        stores.remove(key)?.clear()
    }

    override fun onCleared() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }
}