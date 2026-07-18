package com.github.anrimian.musicplayer.wear.ui.common.error

import android.content.Context
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser

class WearErrorParser(context: Context, private val analytics: Analytics): ErrorParser(context) {

    override fun parseError(throwable: Throwable): ErrorCommand {
        analytics.processNonFatalError(throwable)
        return ErrorCommand(throwable.message ?: "")
    }

    override fun logError(throwable: Throwable) {
        analytics.processNonFatalError(throwable)
    }
}