package com.github.anrimian.musicplayer.ui.common.mvvm

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.effects.MessageAction
import com.github.anrimian.musicplayer.ui.common.effects.MessageDuration
import com.github.anrimian.musicplayer.ui.common.effects.MessageKey
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.toStatedData
import com.github.anrimian.musicplayer.ui.common.navigation.BaseScreen
import com.github.anrimian.musicplayer.ui.common.navigation.CloseScreen
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import com.github.anrimian.musicplayer.domain.utils.coroutines.launchCatching as launchCatchingExt
import com.github.anrimian.musicplayer.domain.utils.coroutines.subscribeCatching as subscribeCatchingExt

abstract class BaseViewModel<S, P : Parcelable>(
    initialState: S,
    initialPersistentState: P,
    protected val savedStateHandle: SavedStateHandle,
    protected val errorParser: ErrorParser
): ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()
    protected val currentState: S
        get() = _state.value

    val persistentState: StateFlow<P> = savedStateHandle.getStateFlow(
        VM_PERSISTENT_KEY,
        initialPersistentState
    )

    val dialogStack: StateFlow<List<AppDialog>> = savedStateHandle.getStateFlow(VM_DIALOGS_KEY, emptyList())

    private val _effects = Channel<BaseEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    protected inline fun <reified T : Parcelable> getArgs(): T {
        return savedStateHandle[VM_ARG_KEY]
            ?: throw IllegalArgumentException("Arguments of type ${T::class.java.simpleName} were expected but not found at key '$VM_ARG_KEY'")
    }

    protected inline fun <reified T : Parcelable> getArgsOrNull(): T? {
        return savedStateHandle[VM_ARG_KEY]
    }

    protected fun <T> getAndClearArg(key: String): T? {
        val value = savedStateHandle.get<T>(key)
        if (value != null) {
            savedStateHandle.remove<T>(key)
        }
        return value
    }

    protected fun updateState(transform: S.() -> S) {
        _state.update(transform)
    }

    protected fun updatePersistent(transform: P.() -> P) {
        val current = persistentState.value
        val new = current.transform()

        if (current != new) {
            savedStateHandle[VM_PERSISTENT_KEY] = new
        }
    }

    protected fun <T> persistentGranularState(key: String, initial: T): MutableStateFlow<T> {
        return savedStateHandle.getStateFlow(key, initial) as MutableStateFlow<T>
    }

    protected fun showDialog(dialog: AppDialog) {
        val currentDialogStack = dialogStack.value
        val top = currentDialogStack.lastOrNull()
        if (top != null && top::class == dialog::class) {
            return
        }
        val mutable = currentDialogStack.toMutableList()
        mutable.add(dialog)
        saveDialogs(mutable)
    }

    protected fun dismissDialog() {
        val current = dialogStack.value.toMutableList()
        if (current.isNotEmpty()) {
            current.removeAt(current.lastIndex)
            saveDialogs(current)
        }
    }

    protected fun dismissDialog(predicate: (AppDialog) -> Boolean) {
        val current = dialogStack.value.toMutableList()
        val changed = current.removeAll(predicate)
        if (changed) {
            saveDialogs(current)
        }
    }

    protected inline fun <reified D : AppDialog> withCurrentDialog(block: (D) -> Unit) {
        val dialog = dialogStack.value.lastOrNull() as? D?
        if (dialog != null) {
            block(dialog)
        }
    }

    protected inline fun <reified D : AppDialog> updateCurrentDialog(block: D.() -> D) {
        val currentStack = dialogStack.value.toMutableList()
        if (currentStack.isEmpty()) {
            return
        }

        val lastIndex = currentStack.lastIndex
        val lastDialog = currentStack[lastIndex]

        if (lastDialog is D) {
            val newDialog = block(lastDialog)
            if (newDialog != lastDialog) {
                currentStack[lastIndex] = newDialog
                saveDialogs(currentStack)
            }
        }
    }

    protected fun sendEffect(effect: BaseEffect) {
        _effects.trySend(effect)
    }

    protected fun sendNavigationEffect(screen: BaseScreen) {
        sendEffect(CommonEffect.NavigationEffect(screen))
    }

    protected fun closeScreen() {
        sendNavigationEffect(CloseScreen)
    }

    protected fun sendMessage(@StringRes resId: Int) {
        sendMessage(message = UiText.StringResource(resId))
    }

    protected fun sendMessage(@StringRes resId: Int, vararg args: Any) {
        sendMessage(message = UiText.StringResource(resId, *args))
    }

    protected fun sendMessage(message: String) {
        sendMessage(message = UiText.DynamicString(message))
    }

    protected fun sendMessage(
        @StringRes messageId: Int,
        @StringRes actionLabelId: Int,
        action: MessageAction
    ) {
        sendMessage(
            message = UiText.StringResource(messageId),
            actionLabel = UiText.StringResource(actionLabelId),
            action = action
        )
    }

    protected fun sendMessage(
        message: String,
        actionLabel: String,
        action: MessageAction
    ) {
        sendMessage(
            message = UiText.DynamicString(message),
            actionLabel = UiText.DynamicString(actionLabel),
            action = action
        )
    }

    protected fun sendMessage(
        message: UiText,
        actionLabel: UiText? = null,
        action: MessageAction? = null,
        duration: MessageDuration = if (action != null) MessageDuration.SystemLong else MessageDuration.SystemShort,
        key: MessageKey? = null
    ) {
        sendEffect(
            CommonEffect.ShowMessage(
                message = message,
                actionLabel = actionLabel,
                action = action,
                duration = duration,
                key = key
            )
        )
    }

    protected fun sendErrorMessage(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        sendErrorMessage(errorCommand)
    }

    protected fun sendErrorMessage(errorCommand: ErrorCommand) {
        sendEffect(CommonEffect.ShowMessage(message = UiText.DynamicString(errorCommand.message)))
    }

    protected fun sendMessage(message: CommonEffect.ShowMessage) {
        sendEffect(message)
    }

    protected fun navigateTo(screen: BaseScreen) {
        sendEffect(CommonEffect.NavigationEffect(screen))
    }

    protected fun launch(
        onError: ((ErrorCommand) -> Unit)? = null,
        onProgress: ((Boolean) -> Unit)? = null,
        scope: CoroutineScope = viewModelScope,
        action: (suspend CoroutineScope.() -> Unit)
    ): Job {
        return launchCatching(
            onError = onError?.let { handler -> { t -> handler(errorParser.parseError(t)) } },
            onProgress,
            scope,
            action
        )
    }

    protected fun launchCatching(
        onError: ((Throwable) -> Unit)? = null,
        onProgress: ((Boolean) -> Unit)? = null,
        scope: CoroutineScope = viewModelScope,
        action: (suspend CoroutineScope.() -> Unit)
    ): Job {
        return scope.launchCatchingExt(
            onError = onError,
            onProgress = onProgress,
            action = action
        )
    }

    protected fun <T> Flow<List<T>>.subscribeStatedList(
        @StringRes emptyMessageId: Int,
        scope: CoroutineScope = viewModelScope,
        reducer: (StatedData<ImmutableList<T>>) -> Unit
    ): Job {
        return this
            .map { list -> list.toImmutableList().toStatedData(emptyMessageId) }
            .subscribeStated(scope = scope, reducer = reducer)
    }

    protected fun <T> Flow<StatedData<T>>.subscribeStated(
        scope: CoroutineScope = viewModelScope,
        onComplete: (() -> Unit)? = null,
        reducer: (StatedData<T>) -> Unit,
    ): Job {
        return this
            .onStart { emit(StatedData.Loading) }
            .subscribe(
                onNext = reducer,
                onError = { cmd -> reducer(StatedData.Error(cmd.message)) },
                onComplete = onComplete,
                scope = scope
            )
    }

    protected fun <T> Flow<T>.subscribe(
        scope: CoroutineScope = viewModelScope,
        onError: ((ErrorCommand) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onProgress: ((Boolean) -> Unit)? = null,
        onNext: ((T) -> Unit)? = null
    ): Job {
        return subscribeCatching(
            onNext = onNext,
            onError = onError?.let { handler -> { t -> handler(errorParser.parseError(t)) } },
            onComplete = onComplete,
            onProgress = onProgress,
            scope = scope
        )
    }

    protected fun <T> Flow<T>.subscribeCatching(
        scope: CoroutineScope = viewModelScope,
        onError: ((Throwable) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onProgress: ((Boolean) -> Unit)? = null,
        onNext: ((T) -> Unit)? = null
    ): Job {
        return subscribeCatchingExt(
            onNext = onNext,
            onError = onError,
            onComplete = onComplete,
            onProgress = onProgress,
            scope = scope
        )
    }

    protected fun saveDialogs(list: List<AppDialog>) {
        savedStateHandle[VM_DIALOGS_KEY] = ArrayList(list)
    }

    companion object {
        const val VM_ARG_KEY = "vm_arg"
        private const val VM_PERSISTENT_KEY = "vm_persistent_state"
        private const val VM_DIALOGS_KEY = "vm_dialogs_stack"
    }

}