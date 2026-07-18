package com.github.anrimian.musicplayer.ui.common.format

import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType

object AppFormatUtils {

    fun parseExtensions(extensions: String): Set<String> {
        return extensions.split(',')
            .mapNotNullTo(HashSet()) { value ->
                value.replace(".", "")
                    .replace("\u200B", "")
                    .trim()
                    .lowercase()
                    .ifEmpty { null }
            }
    }

    fun formatExtensions(extensions: Set<String>): String {
        if (extensions.isEmpty()) {
            return ""
        }
        return extensions.joinToString(prefix = ".", separator = ", \u200B.")
    }

    @Composable
    fun formatArtist(artist: String?): String {
        return if (artist.isNullOrEmpty()) stringResource(R.string.unknown_author) else artist
    }

    @Composable
    fun formatSize(bytes: Long): String {
        val context = LocalContext.current
        return Formatter.formatShortFileSize(context, bytes)
    }

    @Composable
    fun formatCorruptionType(corruptionType: CorruptionType?): String? {
        corruptionType ?: return null
        val messageRes = when (corruptionType) {
            CorruptionType.UNSUPPORTED -> R.string.unsupported_format_hint
            CorruptionType.NOT_FOUND -> R.string.file_not_found
            CorruptionType.NOT_FOUND_IN_ALL_STORAGES -> R.string.file_not_found_in_all_storages
            CorruptionType.TOO_LARGE_SOURCE -> R.string.file_is_too_large
            CorruptionType.FILE_IS_CORRUPTED -> R.string.file_is_corrupted
            CorruptionType.FILE_READ_TIMEOUT -> R.string.file_read_timeout
            CorruptionType.NOT_ALLOWED_PATH -> R.string.not_allowed_path
            else -> R.string.unknown_play_error
        }
        return stringResource(messageRes)
    }



}