package com.github.anrimian.musicplayer.ui.common.locale

import android.app.Activity
import android.content.Context
import java.util.*

interface LocaleController {

    fun dispatchAttachBaseContext(baseContext: Context): Context

    fun setCurrentLocale(locale: Locale?, activity: Activity)

    fun getCurrentLocaleName(): String

    fun openLocaleChooser(activity: Activity)

}