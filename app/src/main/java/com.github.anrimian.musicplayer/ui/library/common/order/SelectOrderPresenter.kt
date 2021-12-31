package com.github.anrimian.musicplayer.ui.library.common.order

import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import moxy.MvpPresenter

class SelectOrderPresenter(
    private val settingsInteractor: DisplaySettingsInteractor
) : MvpPresenter<SelectOrderView>() {

    private lateinit var orderType: OrderType
    private var reverse = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showFileNameEnabled(settingsInteractor.isDisplayFileNameEnabled())
    }

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

    fun onFileNameChecked(checked: Boolean) {
        viewState.showFileNameEnabled(checked)
        settingsInteractor.setDisplayFileName(checked)
    }

    fun onCompleteButtonClicked() {
        viewState.close(Order(orderType, reverse))
    }
}