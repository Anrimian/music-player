package com.github.anrimian.musicplayer.di.app.order

import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderPresenter
import dagger.Module
import dagger.Provides

@Module
class OrderModule(private val order: Order) {

    @Provides
    fun selectOrderPresenter(
        displaySettingsInteractor: DisplaySettingsInteractor
    ) = SelectOrderPresenter(
        order.orderType,
        order.isReversed,
        displaySettingsInteractor
    )
}