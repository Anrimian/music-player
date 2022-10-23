package com.github.anrimian.musicplayer.data.controllers.music.error

import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotFoundException
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser
import com.github.anrimian.musicplayer.domain.models.composition.content.*
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException
import com.google.android.exoplayer2.upstream.ContentDataSource.ContentDataSourceException
import com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException
import com.google.android.exoplayer2.upstream.Loader
import java.io.FileNotFoundException

open class PlayerErrorParserImpl(
    private val analytics: Analytics
): PlayerErrorParser {

    override fun parseError(throwable: Throwable): Throwable {
        when (throwable) {
            is FileNotFoundException -> return LocalSourceNotFoundException()
            is CompositionNotFoundException -> return LocalSourceNotFoundException()
            is PlaybackException -> {
                val cause = throwable.cause
                if (cause is Loader.UnexpectedLoaderException) {
                    val relaunchCause = when (val causeOfCause = cause.cause) {
                        is OutOfMemoryError -> TooLargeSourceException(causeOfCause)
                        else -> cause
                    }
                    return RelaunchSourceException(relaunchCause)
                }
                if (cause is ContentDataSourceException || cause is FileDataSourceException) {
                    val causeOfCause = cause.cause
                    if (causeOfCause is FileNotFoundException) {
                        return LocalSourceNotFoundException()
                    }
                }
                if (cause is UnrecognizedInputFormatException) {
                    return UnsupportedSourceException()
                }
            }
            is UnsupportedSourceException -> return throwable
            is UnavailableMediaStoreException -> return AcceptablePlayerException(throwable)
            is UnknownPlayerException,
            is SecurityException -> {
                analytics.processNonFatalError(throwable)
                return RelaunchSourceException(throwable)
            }
        }
        analytics.processNonFatalError(throwable)
        return throwable
    }
}