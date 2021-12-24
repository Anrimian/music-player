package com.github.anrimian.musicplayer.lite.di.app

import android.content.Context
import com.github.anrimian.musicplayer.lite.ui.SpecialNavigationImpl
import com.github.anrimian.musicplayer.ui.common.navigation.SpecialNavigation
import dagger.Module
import dagger.Provides

@Module
class LiteAppModule(private val appContext: Context) {

    @Provides
    fun provideAppContext(): Context {
        return appContext
    }

    @Provides
    @LiteAppSingleton
    fun navigation(): SpecialNavigation = SpecialNavigationImpl()

}