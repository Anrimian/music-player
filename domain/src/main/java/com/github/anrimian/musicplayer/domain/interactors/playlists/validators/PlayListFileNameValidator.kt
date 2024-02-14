package com.github.anrimian.musicplayer.domain.interactors.playlists.validators

import java.nio.charset.StandardCharsets


object PlayListFileNameValidator {

    private const val MAX_NAME_LENGTH_BYTES = 255
    private const val PLAYLIST_EXTENSION = ".m3u"
    private const val PLAYLIST_NOT_ALLOWED_CHARACTERS = "[\\\\/:*?\"<>|]"

    fun getFormattedPlaylistName(input: String): String {
        val name = normalizePlayListName(input)
        val fileName = getPlaylistFileName(name)
        val utf8Bytes: ByteArray = fileName.toByteArray(StandardCharsets.UTF_8)
        if (utf8Bytes.size > MAX_NAME_LENGTH_BYTES) {
            val extensionRange = PLAYLIST_EXTENSION.toByteArray(StandardCharsets.UTF_8).size
            return String(
                utf8Bytes.copyOfRange(0, MAX_NAME_LENGTH_BYTES - extensionRange),
                StandardCharsets.UTF_8
            )
        }
        return name
    }

    fun isPlaylistNameNotTooLong(input: String): Boolean {
        val name = getPlaylistFileName(input)
        val utf8Bytes: ByteArray = name.toByteArray(StandardCharsets.UTF_8)
        return utf8Bytes.size <= MAX_NAME_LENGTH_BYTES
    }

    fun getPlaylistFileName(input: String): String {
        return input + PLAYLIST_EXTENSION
    }

    fun getPlaylistName(fileName: String): String {
        return fileName.substring(0, fileName.lastIndexOf(PLAYLIST_EXTENSION))
    }

    fun normalizePlayListName(name: String): String {
        return name.replace(PLAYLIST_NOT_ALLOWED_CHARACTERS.toRegex(), "0").trim()
    }

}