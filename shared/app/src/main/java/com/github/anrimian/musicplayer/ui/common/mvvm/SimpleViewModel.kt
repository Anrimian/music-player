package com.github.anrimian.musicplayer.ui.common.mvvm

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser

abstract class SimpleViewModel<S>(
    initialState: S,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : BaseViewModel<S, EmptyPersistent>(
    initialState = initialState,
    initialPersistentState = EmptyPersistent,
    savedStateHandle = savedStateHandle,
    errorParser = errorParser
)