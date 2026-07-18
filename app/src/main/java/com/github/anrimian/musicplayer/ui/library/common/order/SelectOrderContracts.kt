package com.github.anrimian.musicplayer.ui.library.common.order

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class SelectOrderDialogData(
    val selectedOrder: Order? = null,
    val allowedOrderTypes: List<OrderType>,
    val showFileNameSetting: Boolean = true
) : AppDialog

@Immutable
data class SelectOrderState(
    val selectedOrderType: OrderType? = null,
    val isReverse: Boolean,
    val isDisplayFileNameEnabled: Boolean = false
)

sealed interface SelectOrderEffect : BaseEffect {
    data class CloseWithOrder(val order: Order) : SelectOrderEffect
    data object Close : SelectOrderEffect
}
