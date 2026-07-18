package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class M3UEditorTest {

    private val editor = M3UEditor()

    @Test
    fun `read and write test`() {
        val name = "test"
        val createTime = 1000L
        val modifyTime = 2000L
        val entries = listOf(
            PlayListEntry(filePath = "111"),
            PlayListEntry(filePath = "222"),
            PlayListEntry(filePath = "333")
        )
        val playListFile = PlayListFile(name, createTime, modifyTime, entries)
        val baos = ByteArrayOutputStream()
        editor.write(playListFile, baos)

        val rawContent = String(baos.toByteArray())
        println("rawContent: \n$rawContent")

        val readFile = editor.read(name, rawContent.byteInputStream())

        assertEquals(name, readFile.name)
        assertEquals(createTime, readFile.createDate)
        assertEquals(modifyTime, readFile.modifyDate)
        assertEquals(entries, readFile.entries)
    }

}