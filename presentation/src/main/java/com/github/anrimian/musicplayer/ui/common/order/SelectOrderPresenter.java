package com.github.anrimian.musicplayer.ui.common.order;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.composition.order.OrderType;

@InjectViewState
public class SelectOrderPresenter extends MvpPresenter<SelectOrderView> {

    private OrderType orderType;
    private boolean reverse;

    void setOrder(Order order) {
        orderType = order.getOrderType();
        reverse = order.isReversed();
        getViewState().showReverse(reverse);
        getViewState().showSelectedOrder(orderType);
    }

    void onOrderTypeSelected(OrderType order) {
        this.orderType = order;
        getViewState().showSelectedOrder(order);
    }

    void onReverseTypeSelected(boolean selected) {
        this.reverse = selected;
        getViewState().showReverse(selected);
    }

    void onCompleteButtonClicked() {
        getViewState().close(new Order(orderType, reverse));
    }
}
