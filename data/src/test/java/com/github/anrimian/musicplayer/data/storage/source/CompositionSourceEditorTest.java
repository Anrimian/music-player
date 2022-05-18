package com.github.anrimian.musicplayer.data.storage.source;

import static com.github.anrimian.musicplayer.data.utils.files.TestFileUtils.createTempCopy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

public class CompositionSourceEditorTest {

    private String filePath;

    private final StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);
    private final FileSourceProvider fileSourceProvider = mock(FileSourceProvider.class);

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor(musicProvider, fileSourceProvider);

    @BeforeEach
    void setUp(@TempDir File dir) {
        filePath = createTempCopy(dir, "src/test/resources/Кот Леопольд - Неприятность эту мы переживем.mp3").getPath();
    }

    @Test
    public void testEditor() {
        System.out.println("title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());
        System.out.println("author: " + sourceEditor.getCompositionAuthor(filePath).blockingGet());
        System.out.println("album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());
        System.out.println("album artist: " + sourceEditor.getCompositionAlbumArtist(filePath).blockingGet());
        System.out.println("genre: " + sourceEditor.getCompositionGenre(filePath).blockingGet());
    }

    @Test
    public void changeTitleTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        System.out.println("title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());

        String testTitle = "Test title";
        sourceEditor.setCompositionTitle(anyFullComposition(), testTitle).subscribe();
        String newTitle = sourceEditor.getCompositionTitle(filePath).blockingGet();
        System.out.println("new title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());
        assertEquals(testTitle, newTitle);
    }

    @Test
    public void changeAlbumTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        System.out.println("album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());

        String testAlbum = "Test album";
        sourceEditor.setCompositionAlbum(anyFullComposition(), testAlbum).subscribe();
        String newTitle = sourceEditor.getCompositionAlbum(filePath).blockingGet();
        System.out.println("new album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());
        assertEquals(testAlbum, newTitle);
    }

    @Test
    public void addGenreTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        sourceEditor.addCompositionGenre(anyFullComposition(), testGenre1).subscribe();
        sourceEditor.addCompositionGenre(anyFullComposition(), testGenre2).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre1 + "\u0000" + testGenre2, newGenres);
    }

    @Test
    public void removeGenreTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        sourceEditor.addCompositionGenre(anyFullComposition(), testGenre1).subscribe();
        sourceEditor.addCompositionGenre(anyFullComposition(), testGenre2).subscribe();
        sourceEditor.removeCompositionGenre(anyFullComposition(), testGenre1).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre2, newGenres);
    }

    @Test
    public void removeLastTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        sourceEditor.addCompositionGenre(anyFullComposition(), testGenre1).subscribe();
        sourceEditor.removeCompositionGenre(anyFullComposition(), testGenre1).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals("", newGenres);
    }

    @Test
    public void changeGenreTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        String testGenre3 = "Test genre3";
        sourceEditor.addCompositionGenre(anyFullComposition(), testGenre1).subscribe();
        sourceEditor.addCompositionGenre(anyFullComposition(), testGenre2).subscribe();
        sourceEditor.changeCompositionGenre(anyFullComposition(), testGenre1, testGenre3).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre3 + "\u0000" + testGenre2, newGenres);
    }

    @Test
    public void changeAlbumArtistTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        System.out.println("album artist: " + sourceEditor.getCompositionAlbumArtist(filePath).blockingGet());

        String testName = "Test album artist";
        sourceEditor.setCompositionAlbumArtist(anyFullComposition(), testName).subscribe();
        String newArtist = sourceEditor.getCompositionAlbumArtist(filePath).blockingGet();
        System.out.println("new album artist: " + newArtist);
        assertEquals(testName, newArtist);
    }

    @Test
    public void testFileWithWrongEncoding() {
        String filePath = new File("src/test/resources/Back In Black.mp3").getPath();
        System.out.println("title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());
        System.out.println("author: " + sourceEditor.getCompositionAuthor(filePath).blockingGet());
        System.out.println("album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());
        System.out.println("album artist: " + sourceEditor.getCompositionAlbumArtist(filePath).blockingGet());
    }

    private FullComposition anyFullComposition() {
        return new FullComposition(
                null,
                null,
                null,
                null,
                null,
                null,
                0L,
                0L,
                0L,
                0L,
                null,
                null,
                null
        );
    }
}