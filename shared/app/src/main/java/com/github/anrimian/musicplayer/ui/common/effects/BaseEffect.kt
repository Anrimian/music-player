package com.github.anrimian.musicplayer.ui.common.effects


import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalAccessibilityManager
import com.github.anrimian.musicplayer.ui.common.compose.snackbar.showCommonMessage
import com.github.anrimian.musicplayer.ui.common.compose.text.LocalUiTextResolver
import com.github.anrimian.musicplayer.ui.common.navigation.BaseScreen
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

interface BaseEffect

sealed interface CommonEffect : BaseEffect {
    data class NavigationEffect(val screen: BaseScreen) : CommonEffect
    data class ShowMessage(
        val message: UiText,
        val actionLabel: UiText? = null,
        val action: MessageAction? = null,
        val duration: MessageDuration = MessageDuration.SystemShort,
        val key: MessageKey? = null
    ) : CommonEffect
}

interface MessageAction

interface MessageKey

data object DefaultMessageKey : MessageKey

sealed interface MessageDuration {
    data object SystemShort : MessageDuration
    data object SystemLong : MessageDuration
    data object Indefinite : MessageDuration
    data class Custom(val millis: Long) : MessageDuration
}

@Composable
fun ObserveEffects(
    flow: Flow<BaseEffect>,
    snackbarHostState: SnackbarHostState? = null,
    onMessageAction: (MessageAction) -> Unit = {},
    onNavigation: (CommonEffect.NavigationEffect) -> Unit = {},
    onEffect: (BaseEffect) -> Unit = {}
) {
    val resolver = LocalUiTextResolver.current
    val scope = rememberCoroutineScope()
    val accessibilityManager = LocalAccessibilityManager.current
    val activeMessageJobs = remember { mutableMapOf<Any, Job>() }

    val currentOnMessageAction by rememberUpdatedState(onMessageAction)
    val currentOnNavigation by rememberUpdatedState(onNavigation)
    val currentOnEffect by rememberUpdatedState(onEffect)

    LaunchedEffect(Unit) {
        flow.collect { effect ->
            when (effect) {
                is CommonEffect.ShowMessage -> {
                    val messageKey = effect.key ?: DefaultMessageKey

                    activeMessageJobs[messageKey]?.cancel()

                    val job = scope.launch {
                        try {
                            snackbarHostState?.showCommonMessage(
                                event = effect,
                                resolver = resolver,
                                accessibilityManager = accessibilityManager,
                                onAction = { action -> currentOnMessageAction(action) }
                            )
                        } finally {
                            if (activeMessageJobs[messageKey] === coroutineContext.job) {
                                activeMessageJobs.remove(messageKey)
                            }
                        }
                    }
                    activeMessageJobs[messageKey] = job
                }
                is CommonEffect.NavigationEffect -> currentOnNavigation(effect)
                else -> currentOnEffect(effect)
            }
        }
    }
}


