package com.github.anrimian.musicplayer.data.storage.source

import com.github.anrimian.musicplayer.data.storage.exceptions.GenreAlreadyPresentException
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
import java.util.Arrays

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
        println("genre: " + sourceEditor.getCompositionRawGenre(source))
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
    fun addGenreInFirstPositionTest() {
        val genres = sourceEditor.getCompositionRawGenre(source)
        println("genres: $genres")

        val testGenre2 = "Test genre2"
        val testGenre3 = "Test genre3"
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre3).subscribe()
        val testGenre1 = "Test genre1"
        sourceEditor.addCompositionGenre(source, testGenre1, 0).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(3, newGenres.size)
        assertEquals(testGenre1, newGenres[0])
        assertEquals(testGenre2, newGenres[1])
        assertEquals(testGenre3, newGenres[2])
    }

    @Test
    fun addGenreInMiddlePositionTest() {
        val genres = sourceEditor.getCompositionRawGenre(source)
        println("genres: $genres")

        val testGenre1 = "Test genre1"
        val testGenre3 = "Test genre3"
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe()
        sourceEditor.addCompositionGenre(source, testGenre3).subscribe()
        val testGenre2 = "Test genre2"
        sourceEditor.addCompositionGenre(source, testGenre2, 1).subscribe()

        val newGenres = sourceEditor.getCompositionGenres(source)
        println("new genres: " + Arrays.toString(newGenres))
        assertEquals(3, newGenres.size)
        assertEquals(testGenre1, newGenres[0])
        assertEquals(testGenre2, newGenres[1])
        assertEquals(testGenre3, newGenres[2])
    }

    @ParameterizedTest
    @CsvSource(
        "Test genre1, Test genre2, 'Test genre1, Test genre2'",
        "'Test genre1, Test genre2', Test genre3, 'Test genre1, Test genre2, Test genre3'",
        "'Test genre1', Test, 'Test genre1, Test'",
        //spaces case
        "'', 'Test genre1 ', Test genre1",
        "'Test genre1', ' Test genre2', 'Test genre1, Test genre2'",
    )
    fun addGenreTest(
        rawGenre: String,
        genreToAdd: String,
        expectedGenres: String
    ) {
        sourceEditor.setCompositionRawGenre(source, rawGenre).subscribe()
        sourceEditor.addCompositionGenre(source, genreToAdd).subscribe()

        assertEquals(expectedGenres, sourceEditor.getCompositionRawGenre(source))
    }

    @ParameterizedTest
    @CsvSource(
        "Test genre1, Test genre1, Test genre1",
    )
    fun addDuplicateGenreTest(
        rawGenre: String,
        genreToAdd: String,
        expectedGenres: String
    ) {
        sourceEditor.setCompositionRawGenre(source, rawGenre).subscribe()
        sourceEditor.addCompositionGenre(source, genreToAdd)
            .test()
            .assertError { t -> t is GenreAlreadyPresentException }

        assertEquals(expectedGenres, sourceEditor.getCompositionRawGenre(source))
    }

    @ParameterizedTest
    @CsvSource(
        "'Test genre1, Test genre2', 0, 1, 'Test genre2, Test genre1'",
        "'Test genre1, Test genre2', 1, 0, 'Test genre2, Test genre1'",
        "'Test genre1, Test genre2, Test genre3', 1, 0, 'Test genre2, Test genre1, Test genre3'",
        "'Test genre1, Test genre2, Test genre3', 1, 2, 'Test genre1, Test genre3, Test genre2'",
        "'Test genre1, Test genre2, Test genre3', 0, 2, 'Test genre2, Test genre3, Test genre1'",
        "'Test genre1, Test genre2, Test genre3, Test genre4', 1, 2, 'Test genre1, Test genre3, Test genre2, Test genre4'",
        "'Test genre1, Test genre2, Test genre3, Test genre4', 3, 0, 'Test genre4, Test genre1, Test genre2, Test genre3'",
        "'Test genre1, Test genre2, Test genre3, Test genre4', 0, 3, 'Test genre2, Test genre3, Test genre4, Test genre1'",
    )
    fun moveGenreTest(
        rawGenre: String,
        from: Int,
        to: Int,
        expectedGenres: String
    ) {
        sourceEditor.setCompositionRawGenre(source, rawGenre).subscribe()
        sourceEditor.moveGenre(source, from, to).subscribe()

        assertEquals(expectedGenres, sourceEditor.getCompositionRawGenre(source))
    }

    @ParameterizedTest
    @CsvSource(
        "'Test genre1, Test genre2', Test genre1, Test genre2",
        "'Test genre1, Test genre2, Test genre3', Test genre2, 'Test genre1, Test genre3'",
        "'Test genre1, Test genre2', Test genre2, Test genre1",
        "Test genre1, Test genre1, ''",
        "'Test genre1, Test', Test, Test genre1",
        //invalid values cases
        "Test genre1k, Test genre1, Test genre1k",
        "Test genre1kk, Test genre1, Test genre1kk",
        "kTest genre1, Test genre1, kTest genre1",
        "kkTest genre1, Test genre1, kkTest genre1",
        //duplicate cases
        "'Test genre1, Test genre1', Test genre1, ''",
        "'Test genre0, Test genre1, Test genre1', Test genre1, Test genre0",
        "'Test genre1, Test genre0, Test genre1', Test genre1, Test genre0",
        "'Test genre1, Test genre1, Test genre0', Test genre1, Test genre0",
    )
    fun removeGenreTest(
        rawGenre: String,
        genreToRemove: String,
        expectedGenresLeft: String
    ) {
        sourceEditor.setCompositionRawGenre(source, rawGenre).subscribe()
        sourceEditor.removeCompositionGenre(source, genreToRemove).subscribe()

        assertEquals(expectedGenresLeft, sourceEditor.getCompositionRawGenre(source))
    }

    @ParameterizedTest
    @CsvSource(
        "'Test genre1, Test genre2, Test genre3', Test genre1, Test genre1_a, 'Test genre1_a, Test genre2, Test genre3'",
        "'Test genre1, Test genre2', Test genre1, Test, 'Test, Test genre2'",
        )
    fun changeGenreTest(
        rawGenre: String,
        genreToChange: String,
        newGenre: String,
        expectedGenresLeft: String
    ) {
        println("before: $rawGenre")
        println("replace: $genreToChange -> $newGenre")
        sourceEditor.setCompositionRawGenre(source, rawGenre).subscribe()
        sourceEditor.changeCompositionGenre(source, genreToChange, newGenre).subscribe()

        val result = sourceEditor.getCompositionRawGenre(source)
        println("after: $result")
        assertEquals(expectedGenresLeft, result)
    }

    @ParameterizedTest
    @CsvSource(
        "'Test genre1, Test genre2', Test genre1, Test genre2",
        "'Test genre1, Test genre2, Test genre3', Test genre1, Test genre2",
        "'Test genre2, Test genre1, Test genre3', Test genre1, Test genre2",
        "'Test genre2, Test genre3, Test genre1', Test genre1, Test genre2",
        "'Test genre2, Test genre1, Test genre2', Test genre1, Test genre2",
    )
    fun changeGenreToDuplicateTest(
        rawGenre: String,
        genreToChange: String,
        newGenre: String,
    ) {
        println("before: $rawGenre")
        println("replace: $genreToChange -> $newGenre")
        sourceEditor.setCompositionRawGenre(source, rawGenre).subscribe()
        sourceEditor.changeCompositionGenre(source, genreToChange, newGenre)
            .test()
            .assertError { t -> t is GenreAlreadyPresentException }
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