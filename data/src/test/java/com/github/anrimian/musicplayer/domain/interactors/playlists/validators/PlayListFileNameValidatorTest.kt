package com.github.anrimian.musicplayer.domain.interactors.playlists.validators

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PlayListFileNameValidatorTest {

    @Test
    fun testGetFormattedPlaylistName(@TempDir dir: File) {
        val name = "আমার-সোনার-বাংলা-আমি-তোমায়-ভালোবাসি-চিরদিন-তোমার-আকাশ-তোমার-বাতাস-আমার-প্রাণে-বাজায়-বাঁশি-ও-মা-ফাগুনে-তোর"
        val formattedName = PlayListFileNameValidator.getFormattedPlaylistName(name)
        assertNotEquals(name, formattedName)
        val file = File(dir, formattedName)
        assertTrue(file.createNewFile())
    }

    @Test
    fun testGetFormattedPlaylistNameWithSymbols(@TempDir dir: File) {
        val name = "আমার/-সোনার-বাংলা-আমি-তোমায়-ভালোবাসি-চিরদিন-তোমার-আকাশ-তোমার-বাতাস-আমার-প্রাণে-বাজায়-বাঁশি-ও-মা-ফাগুনে-তোর"
        val formattedName = PlayListFileNameValidator.getFormattedPlaylistName(name)
        assertNotEquals(name, formattedName)
        val file = File(dir, formattedName)
        assertTrue(file.createNewFile())
    }

    @Test
    fun testGetFormattedPlaylistNameWithOnlyForbiddenSymbols(@TempDir dir: File) {
        val name = "/"
        val formattedName = PlayListFileNameValidator.getFormattedPlaylistName(name)
        assertNotEquals(name, formattedName)
        val file = File(dir, formattedName)
        assertTrue(file.createNewFile())
    }

    @Test
    fun testIsPlaylistNameNotTooLong() {
        val name = "আমার-সোনার-বাংলা-আমি-তোমায়-ভালোবাসি-চিরদিন-তোমার-আকাশ-তোমার-বাতাস-আমার-প্রাণে-বাজায়-বাঁশি-ও-মা-ফাগুনে-তোর.m3u"
        assertFalse(PlayListFileNameValidator.isPlaylistNameNotTooLong(name))
    }

}