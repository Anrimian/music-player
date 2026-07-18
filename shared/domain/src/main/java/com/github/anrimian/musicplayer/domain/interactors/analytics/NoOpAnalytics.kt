package com.github.anrimian.musicplayer.domain.interactors.analytics

object NoOpAnalytics : Analytics {

    override fun processNonFatalError(throwable: Throwable) {}

    override fun processNonFatalError(throwable: Throwable, message: String?) {}

    override fun logMessage(message: String) {}
}
