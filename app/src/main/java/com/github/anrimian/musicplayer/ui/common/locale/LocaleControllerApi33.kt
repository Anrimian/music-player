package com.github.anrimian.musicplayer.ui.common.locale

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.anrimian.musicplayer.ui.utils.startAppLocaleSettings
import java.util.*

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class LocaleControllerApi33: LocaleController {

    override fun dispatchAttachBaseContext(baseContext: Context) = baseContext

    override fun setCurrentLocale(locale: Locale?, activity: Activity) {}

    override fun getCurrentLocaleName(): String {
        return Locale.getDefault().displayName
    }

    override fun openLocaleChooser(activity: Activity) {
        startAppLocaleSettings(activity)
    }
}