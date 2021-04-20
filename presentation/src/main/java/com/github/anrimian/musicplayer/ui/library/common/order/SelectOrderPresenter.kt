package com.github.anrimian.musicplayer.ui.library.common.order

import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import moxy.MvpPresenter

class SelectOrderPresenter : MvpPresenter<SelectOrderView>() {

    private lateinit var orderType: OrderType
    private var reverse = false

    fun setOrder(order: Order) {
        orderType = order.orderType
        reverse = order.isReversed
        viewState.showReverse(reverse)
        viewState.showSelectedOrder(orderType)
    }

    fun onOrderTypeSelected(order: OrderType) {
        orderType = order
        viewState.showSelectedOrder(order)
    }

    fun onReverseTypeSelected(selected: Boolean) {
        reverse = selected
        viewState.showReverse(selected)
    }

    fun onCompleteButtonClicked() {
        viewState.close(Order(orderType, reverse))
    }
}