package com.github.anrimian.musicplayer.di.app.share

import com.github.anrimian.musicplayer.ui.common.dialogs.share.ShareCompositionsPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ ShareModule::class ])
interface ShareComponent {

    fun shareCompositionsPresenter(): ShareCompositionsPresenter

}