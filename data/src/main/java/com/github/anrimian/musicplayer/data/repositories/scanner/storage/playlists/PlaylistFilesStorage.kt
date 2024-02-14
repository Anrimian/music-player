package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists

import android.content.Context
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.M3UEditor
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlayListFileNameValidator
import java.io.File

class PlaylistFilesStorage(context: Context, private val analytics: Analytics) {

    private val playlistsFilesDir = File(context.filesDir, PlAY_LISTS_DIRECTORY_NAME)

    private val m3uEditor = M3UEditor()

    fun getCachedPlaylists(): List<PlayListFile> {
        val files = getPlaylistFilesDir().listFiles() ?: return emptyList()
        return files.mapNotNull { file ->
            try {
                return@mapNotNull file.inputStream().use { stream ->
                    m3uEditor.read(PlayListFileNameValidator.getPlaylistName(file.name), stream)
                }
            } catch (e: Exception) {
                analytics.processNonFatalError(e, "playlist file '${file.name}' (length: ${file.length()}) read error")
                file.delete()
                return@mapNotNull null
            }

        }
    }

    fun getPlaylistFile(name: String): File {
        val formattedName = name.trim().ifEmpty { "0" }//fix for broken migration(15). Move fix to next migration and remove it
        return File(getPlaylistFilesDir(), PlayListFileNameValidator.getPlaylistFileName(formattedName))
    }

    fun insertPlaylist(playList: PlayListFile) {
        val file = getPlaylistFile(playList.name)
        try {
            file.outputStream().use { stream -> m3uEditor.write(playList, stream) }
        } catch (e: Exception) {
            analytics.logMessage("playlist file (${file.absolutePath}) insert error")
        }
    }

    fun renamePlaylist(name: String, newName: String) {
        val file = getPlaylistFile(name)
        val newFile = getPlaylistFile(newName)
        file.renameTo(newFile)
    }

    fun deletePlayList(name: String) {
        val file = getPlaylistFile(name)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getPlaylistFilesDir(): File {
        if (!playlistsFilesDir.exists()) {
            playlistsFilesDir.mkdirs()
        }
        return playlistsFilesDir
    }

    private companion object {
        const val PlAY_LISTS_DIRECTORY_NAME = "playlists"
    }

}
