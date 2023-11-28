package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists

import android.content.Context
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.M3UEditor
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import java.io.File

private const val PlAY_LISTS_DIRECTORY_NAME = "playlists"

class PlaylistFilesStorage(context: Context, private val analytics: Analytics) {

    private val playlistsFilesDir = File(context.filesDir, PlAY_LISTS_DIRECTORY_NAME)

    private val m3uEditor = M3UEditor()

    fun getCachedPlaylists(): List<PlayListFile> {
        val files = getPlaylistFilesDir().listFiles() ?: return emptyList()
        return files.mapNotNull { file ->
            try {
                return@mapNotNull file.inputStream().use { stream ->
                    m3uEditor.read(getPlaylistName(file.name), stream)
                }
            } catch (e: Exception) {
                analytics.processNonFatalError(e, "playlist file '${file.name}' (length: ${file.length()}) read error")
                file.delete()
                return@mapNotNull null
            }

        }
    }

    fun getPlaylistFile(name: String): File {
        return File(getPlaylistFilesDir(), getPlaylistFileName(name))
    }

    fun insertPlaylist(playList: PlayListFile) {
        val file = getPlaylistFile(playList.name)
        try {
            file.outputStream().use { stream -> m3uEditor.write(playList, stream) }
        } catch (e: Exception) {
            analytics.logMessage("playlist file (${file.name}) insert error")
            throw e
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

    private fun getPlaylistName(fileName: String): String {
        return fileName.substring(0, fileName.lastIndexOf(".m3u"))
            .take(Constants.PLAYLIST_NAME_MAX_LENGTH)//fix for too long names that were already inserted before limitations
    }

    private fun getPlaylistFileName(playlistName: String): String {
        return playlistName + Constants.PLAYLIST_EXTENSION
    }
}
