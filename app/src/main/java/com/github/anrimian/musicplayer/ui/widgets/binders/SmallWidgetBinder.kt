package com.github.anrimian.musicplayer.ui.widgets.binders

import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.widgets.providers.WidgetProviderSmall

class SmallWidgetBinder: WidgetBinder() {

    override fun getWidgetProviderClass(): Class<*> = WidgetProviderSmall::class.java

    override fun getRemoteViewId() = R.layout.widget_small
}