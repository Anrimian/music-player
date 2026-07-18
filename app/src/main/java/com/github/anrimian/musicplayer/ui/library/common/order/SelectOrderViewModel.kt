package com.github.anrimian.musicplayer.ui.library.common.order

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel

class SelectOrderViewModel(
    private val displaySettingsInteractor: DisplaySettingsInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : SimpleViewModel<SelectOrderState>(
    initialState = SelectOrderState(
        isReverse = false
    ),
    savedStateHandle = savedStateHandle,
    errorParser = errorParser
) {

    private val data = getArgs<SelectOrderDialogData>()

    init {
        updateState { copy(
            selectedOrderType = data.selectedOrder?.orderType,
            isReverse = data.selectedOrder?.isReversed ?: false,
            isDisplayFileNameEnabled = displaySettingsInteractor.isDisplayFileNameEnabled()
        ) }
    }

    fun onOrderTypeSelected(orderType: OrderType) {
        updateState { copy(selectedOrderType = orderType) }
    }

    fun onReverseChanged(isReverse: Boolean) {
        updateState { copy(isReverse = isReverse) }
    }

    fun onDisplayFileNameChanged(isEnabled: Boolean) {
        updateState { copy(isDisplayFileNameEnabled = isEnabled) }
        displaySettingsInteractor.setDisplayFileName(isEnabled)
    }

    fun onCompleteButtonClicked() {
        val selectedOrderType = currentState.selectedOrderType ?: return
        val order = Order(selectedOrderType, currentState.isReverse)
        sendEffect(SelectOrderEffect.CloseWithOrder(order))
    }

}
