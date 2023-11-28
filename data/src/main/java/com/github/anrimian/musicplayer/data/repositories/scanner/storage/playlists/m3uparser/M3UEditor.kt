package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.util.Date

private const val EDITOR_VERSION = 1

private const val M3U_START_MARKER = "#EXTM3U"
private const val M3U_VERSION = "#EXT-VERSION:"
private const val M3U_CREATE_DATE = "#EXT-CREATE-DATE:"
private const val M3U_MODIFY_DATE = "#EXT-MODIFY-DATE:"
private const val M3U_INFO_MARKER = "#EXTINF:"

//example: https://github.com/thomasgermain/m3u-parser/blob/master/src/main/java/be/tgermain/m3uparser/core/Parser.java
class M3UEditor {

    fun write(playlistFile: PlayListFile, stream: OutputStream) {
        val pw = PrintWriter(stream)
        pw.println(M3U_START_MARKER)
        pw.println(M3U_VERSION + EDITOR_VERSION)
        pw.println(M3U_CREATE_DATE + playlistFile.createDate.time)
        pw.println(M3U_MODIFY_DATE + playlistFile.modifyDate.time)
        for(entry in playlistFile.entries) {
            pw.println(entry.filePath)
        }
        pw.flush()
    }

    fun read(fileName: String, stream: InputStream): PlayListFile {
        var createTime = System.currentTimeMillis()
        var modifyTime = System.currentTimeMillis()
        val entries = ArrayList<PlayListEntry>()

        val buffer = BufferedReader(InputStreamReader(stream))
        var lineStr = buffer.readLine()
        if (!lineStr.containsOpt(M3U_START_MARKER)) {
            throw M3UEditorException("First line of the file should be $M3U_START_MARKER")
        }
        lineStr = buffer.readLine()
        val versionIndex = lineStr.indexOfOpt(M3U_VERSION)
        if (versionIndex >= 0) {
            //don't do anything with version for now
            lineStr = buffer.readLine()
        }
        val createTimeIndex = lineStr.indexOfOpt(M3U_CREATE_DATE)
        if (createTimeIndex >= 0) {
            createTime = lineStr.substring(createTimeIndex + M3U_CREATE_DATE.length).toLong()
            lineStr = buffer.readLine()
        }
        val modifyTimeIndex = lineStr.indexOfOpt(M3U_MODIFY_DATE)
        if (modifyTimeIndex >= 0) {
            modifyTime = lineStr.substring(modifyTimeIndex + M3U_MODIFY_DATE.length).toLong()
            lineStr = buffer.readLine()
        }
        while (lineStr != null) {
            if (lineStr.contains(M3U_INFO_MARKER)) {
                lineStr = buffer.readLine()
                continue//skip info, read only file paths
            }
            entries.add(PlayListEntry(lineStr))
            lineStr = buffer.readLine()
        }

        return PlayListFile(
            fileName,
            Date(createTime),
            Date(modifyTime),
            entries
        )
    }

    private fun String?.containsOpt(other: String): Boolean {
        return this?.contains(other) == true
    }

    private fun String?.indexOfOpt(other: String): Int {
        return this?.indexOf(other) ?: -1
    }

}