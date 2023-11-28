package com.github.anrimian.musicplayer.di.app.order

import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ OrderModule::class ])
interface OrderComponent {

    fun selectOrderPresenter(): SelectOrderPresenter

}