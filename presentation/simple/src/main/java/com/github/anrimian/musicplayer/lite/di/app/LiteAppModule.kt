package com.github.anrimian.musicplayer.lite.di.app

import com.github.anrimian.musicplayer.lite.ui.SpecialNavigationImpl
import com.github.anrimian.musicplayer.ui.common.navigation.SpecialNavigation
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LiteAppModule {

    @Provides
    @Singleton
    fun navigation(): SpecialNavigation = SpecialNavigationImpl()

}