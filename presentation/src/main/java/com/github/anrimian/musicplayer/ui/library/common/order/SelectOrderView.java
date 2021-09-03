package com.github.anrimian.musicplayer.ui.library.common.order;

import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.Skip;

public interface SelectOrderView extends MvpView {

    @Skip
    void close(Order order);

    @AddToEndSingle
    void showSelectedOrder(OrderType order);

    @AddToEndSingle
    void showReverse(boolean selected);

    @AddToEndSingle
    void showFileNameEnabled(boolean checked);
}
