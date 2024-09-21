package com.github.anrimian.musicplayer.ui.common.activity

import android.content.Context
import com.github.anrimian.musicplayer.di.Components
import moxy.MvpAppCompatActivity

open class BaseMvpAppCompatActivity : MvpAppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(
            Components.getAppComponent().localeController().dispatchAttachBaseContext(base)
        )
    }

}