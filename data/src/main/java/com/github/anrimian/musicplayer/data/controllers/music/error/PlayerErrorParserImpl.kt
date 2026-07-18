package com.github.anrimian.musicplayer.data.controllers.music.error

import androidx.annotation.OptIn
import androidx.media3.common.ParserException
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ContentDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import androidx.media3.exoplayer.upstream.Loader
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotFoundException
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser
import com.github.anrimian.musicplayer.domain.models.composition.content.AcceptablePlayerException
import com.github.anrimian.musicplayer.domain.models.composition.content.CorruptedMediaFileException
import com.github.anrimian.musicplayer.domain.models.composition.content.LocalSourceNotFoundException
import com.github.anrimian.musicplayer.domain.models.composition.content.NoReadPermissionException
import com.github.anrimian.musicplayer.domain.models.composition.content.RelaunchSourceException
import com.github.anrimian.musicplayer.domain.models.composition.content.TooLargeSourceException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnknownPlayerException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.exceptions.NotAllowedPathException
import java.io.FileNotFoundException

open class PlayerErrorParserImpl(
    private val analytics: Analytics,
): PlayerErrorParser {

    @OptIn(UnstableApi::class)
    override fun parseError(throwable: Throwable): Throwable {
        when (throwable) {
            is RelaunchSourceException -> return throwable
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
                if (cause is ContentDataSource.ContentDataSourceException || cause is FileDataSource.FileDataSourceException) {
                    val causeOfCause = cause.cause
                    if (causeOfCause is FileNotFoundException) {
                        return LocalSourceNotFoundException()
                    }
                }
                if (cause is UnrecognizedInputFormatException) {
                    return UnsupportedSourceException()
                }
                if (cause is ParserException) {
                    return CorruptedMediaFileException()
                }
                if (isTransientCodecError(throwable)) {
                    return RelaunchSourceException(throwable)
                }
            }
            is UnsupportedSourceException -> return throwable
            is NoReadPermissionException -> return throwable
            is UnavailableMediaStoreException -> return AcceptablePlayerException(throwable)
            is NotAllowedPathException -> return throwable
            is UnknownPlayerException,
            is SecurityException,
                -> {
                analytics.processNonFatalError(throwable)
                return RelaunchSourceException(throwable)
            }
        }
        analytics.processNonFatalError(throwable)
        return throwable
    }

    private fun isTransientCodecError(throwable: Throwable): Boolean {
        var cause: Throwable? = throwable
        while (cause != null) {
            if (cause is android.media.MediaCodec.CodecException) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
}