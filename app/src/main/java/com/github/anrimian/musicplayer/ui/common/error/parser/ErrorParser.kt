package com.github.anrimian.musicplayer.ui.common.error.parser

import android.content.Context
import androidx.annotation.StringRes
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand

/**
 * Created on 29.10.2017.
 */
abstract class ErrorParser(private val context: Context) {

    protected fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    protected fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    protected fun error(@StringRes resId: Int): ErrorCommand {
        return ErrorCommand(getString(resId))
    }

    abstract fun parseError(throwable: Throwable): ErrorCommand

    abstract fun logError(throwable: Throwable)

}