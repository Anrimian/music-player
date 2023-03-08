package com.github.anrimian.musicplayer.data.storage.source

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.data.utils.files.TestFileUtils
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.util.*

class CompositionSourceEditorTest {
    
    private var filePath: String? = null

    private val musicProvider = mock<StorageMusicProvider>()
    private val fileSourceProvider = mock<FileSourceProvider>()
    private val contentSourceHelper = mock<ContentSourceHelper>()

    private val sourceEditor = CompositionSourceEditor(
        musicProvider,
        fileSourceProvider,
        contentSourceHelper
    )
    private val source = mock<CompositionContentSource>()

    @BeforeEach
    fun setUp(@TempDir dir: File?) {
        val file = TestFileUtils.createTempCopy(dir, "src/test/resources/Кот Леопольд - Неприятность эту мы переживем.mp3")
        filePath = file.path
        whenever(contentSourceHelper.getAsFile(anyOrNull())).thenReturn(file)
        whenever(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath)
    }

    @Test
    fun testEditor() {
        val audioFileInfo = sourceEditor.getAudioFileInfo(source).blockingGet()
        val tags = audioFileInfo.audioTags
        println("title: " + tags.title)
        println("author: " + tags.artist)
        println("album: " + tags.album)
        println("album artist: " + tags.albumArtist)
        println("durationSeconds: " + tags.durationSeconds)
        println("genre: " + sourceEditor.getCompositionGenre(source))
    }

    @Test
    fun changeTitleTest() {
        println("title: " + sourceEditor.getCompositionTitle(source))
        val testTitle = "Test title"
        sourceEditor.setCompositionTitle(source, testTitle).subscribe()
        val newTitle = sourceEditor.getCompositionTitle(source)
        println("new title: " + sourceEditor.getCompositionTitle(source))
        assertEquals(testTitle, newTitle)
    }

    @Test
    fun changeAlbumTest() {
        println("album: " + sourceEditor.getCompositionAlbum(source))
        val testAlbum = "Test album"
        sourceEditor.setCompositionAlbum(source, testAlbum).subscribe()
        val newTitle = sourceEditor.getCompositionAlbum(source)
        println("new album: " + sourceEditor.getCompositionAlbum(source))
        assertEquals(testAlbum, newTitle)
    }

    @Test
    fun addGenreTest() {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        val testGenre2 = "Test genre2"
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(testGenre1, newGenres[0])
        assertEquals(testGenre2, newGenres[1])
    }

    @Test
    fun addGenreWithSpacesTest() {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        val testGenre2 = "Test genre2"
        sourceEditor.addCompositionGenre(source, "$testGenre1 ").subscribe()
        sourceEditor.addCompositionGenre(source, " $testGenre2").subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(testGenre1, newGenres[0])
        assertEquals(testGenre2, newGenres[1])
    }

    @Test
    fun removeGenreAtFirstPositionTest() {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        val testGenre2 = "Test genre2"
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe()
        sourceEditor.removeCompositionGenre(source, testGenre1).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(1, newGenres.size)
        assertEquals(testGenre2, newGenres[0])
    }

    @Test
    fun removeGenreInMiddlePositionTest() {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        val testGenre2 = "Test genre2"
        val testGenre3 = "Test genre3"
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre3).subscribe()
        sourceEditor.removeCompositionGenre(source, testGenre2).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(2, newGenres.size)
        assertEquals(testGenre1, newGenres[0])
        assertEquals(testGenre3, newGenres[1])
    }

    @Test
    fun removeGenreOnLastPositionTest() {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        val testGenre2 = "Test genre2"
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe()
        sourceEditor.removeCompositionGenre(source, testGenre2).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(1, newGenres.size)
        assertEquals(testGenre1, newGenres[0])
    }

    @Test
    fun removeLastGenreTest() {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe()
        sourceEditor.removeCompositionGenre(source, testGenre1).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(0, newGenres.size)
    }

    @ParameterizedTest
    @CsvSource(
        "Test genre1k, Test genre1, k",
        "Test genre1kk, Test genre1, kk",
        "kTest genre1, Test genre1, k",
        "kkTest genre1, Test genre1, kk",
    )
    fun removeInvalidGenreTest(genre: String, genreToRemote: String, expectedSymbolsLeft: String) {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        sourceEditor.addCompositionGenre(source, genre).subscribe()
        sourceEditor.removeCompositionGenre(source, genreToRemote).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(1, newGenres.size)
        assertEquals(expectedSymbolsLeft, newGenres[0])
    }

    @Test
    fun changeGenreTest() {
        val genres = sourceEditor.getCompositionGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        val testGenre2 = "Test genre2"
        val testGenre3 = "Test genre3"
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe()
        sourceEditor.changeCompositionGenre(source, testGenre1, testGenre3).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(testGenre3, newGenres[0])
        assertEquals(testGenre2, newGenres[1])
    }

    @Test
    fun changeAlbumArtistTest() {
        println("album artist: " + sourceEditor.getCompositionAlbumArtist(source))
        val testName = "Test album artist"
        sourceEditor.setCompositionAlbumArtist(source, testName).subscribe()
        val newArtist = sourceEditor.getCompositionAlbumArtist(source)
        println("new album artist: $newArtist")
        assertEquals(testName, newArtist)
    }

    @Test
    fun testFileWithWrongEncoding() {
        val file = File("src/test/resources/Back In Black.mp3")
        whenever(contentSourceHelper.getAsFile(anyOrNull())).thenReturn(file)
        println("title: " + sourceEditor.getCompositionTitle(source))
        println("author: " + sourceEditor.getCompositionAuthor(source))
        println("album: " + sourceEditor.getCompositionAlbum(source))
        println("album artist: " + sourceEditor.getCompositionAlbumArtist(source))
    }
}