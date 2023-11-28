package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.Date

class M3UEditorTest {

    private val editor = M3UEditor()

    @Test
    fun `read and write test`() {
        val name = "test"
        val createDate = Date(1000)
        val modifyDate = Date(2000)
        val entries = listOf(
            PlayListEntry(filePath = "111"),
            PlayListEntry(filePath = "222"),
            PlayListEntry(filePath = "333")
        )
        val playListFile = PlayListFile(name, createDate, modifyDate, entries)
        val baos = ByteArrayOutputStream()
        editor.write(playListFile, baos)

        val rawContent = String(baos.toByteArray())
        println("rawContent: \n$rawContent")

        val readFile = editor.read(name, rawContent.byteInputStream())

        assertEquals(name, readFile.name)
        assertEquals(createDate, readFile.createDate)
        assertEquals(modifyDate, readFile.modifyDate)
        assertEquals(entries, readFile.entries)
    }

}