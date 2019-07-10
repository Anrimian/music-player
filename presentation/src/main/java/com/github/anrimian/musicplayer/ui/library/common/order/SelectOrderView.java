package com.github.anrimian.musicplayer.ui.library.common.order;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.composition.order.OrderType;

public interface SelectOrderView extends MvpView {

    @StateStrategyType(SkipStrategy.class)
    void close(Order order);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSelectedOrder(OrderType order);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showReverse(boolean selected);
}
