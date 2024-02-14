package com.github.anrimian.musicplayer.ui.library.common.order

import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

interface SelectOrderView : MvpView {

    @Skip
    fun close(order: Order)

    @AddToEndSingle
    fun showSelectedOrder(orderType: OrderType)

    @AddToEndSingle
    fun showReverse(selected: Boolean)

    @AddToEndSingle
    fun showFileNameEnabled(checked: Boolean)

}