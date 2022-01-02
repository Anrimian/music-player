package com.github.anrimian.musicplayer.ui.library.compositions

import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView
import moxy.viewstate.strategy.alias.OneExecution

interface LibraryCompositionsView : BaseLibraryCompositionsView {

    @OneExecution
    fun showSelectOrderScreen(order: Order)

}