package com.github.anrimian.musicplayer.data.storage.source;

import static com.github.anrimian.musicplayer.data.utils.files.TestFileUtils.createTempCopy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource;
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileInfo;
import com.github.anrimian.musicplayer.domain.models.composition.tags.CompositionSourceTags;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

public class CompositionSourceEditorTest {

    private String filePath;

    private final StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);
    private final FileSourceProvider fileSourceProvider = mock(FileSourceProvider.class);
    private final ContentSourceHelper contentSourceHelper = mock(ContentSourceHelper.class);

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor(
            musicProvider,
            fileSourceProvider,
            contentSourceHelper
    );

    private final CompositionContentSource source = mock(CompositionContentSource.class);

    @BeforeEach
    void setUp(@TempDir File dir) {
        File file = createTempCopy(dir, "src/test/resources/Кот Леопольд - Неприятность эту мы переживем.mp3");
        filePath = file.getPath();

        when(contentSourceHelper.getAsFile(any())).thenReturn(file);
    }

    @Test
    public void testEditor() {
        AudioFileInfo audioFileInfo = sourceEditor.getAudioFileInfo(source).blockingGet();
        CompositionSourceTags tags = audioFileInfo.getAudioTags();
        System.out.println("title: " + tags.getTitle());
        System.out.println("author: " + tags.getArtist());
        System.out.println("album: " + tags.getAlbum());
        System.out.println("album artist: " + tags.getAlbumArtist());
        System.out.println("durationSeconds: " + tags.getDurationSeconds());
        System.out.println("genre: " + sourceEditor.getCompositionGenre(source));
    }

    @Test
    public void changeTitleTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        System.out.println("title: " +  sourceEditor.getCompositionTitle(source));

        String testTitle = "Test title";
        sourceEditor.setCompositionTitle(source, testTitle).subscribe();
        String newTitle =  sourceEditor.getCompositionTitle(source);
        System.out.println("new title: " +  sourceEditor.getCompositionTitle(source));
        assertEquals(testTitle, newTitle);
    }

    @Test
    public void changeAlbumTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        System.out.println("album: " + sourceEditor.getCompositionAlbum(source));

        String testAlbum = "Test album";
        sourceEditor.setCompositionAlbum(source, testAlbum).subscribe();
        String newTitle = sourceEditor.getCompositionAlbum(source);
        System.out.println("new album: " + sourceEditor.getCompositionAlbum(source));
        assertEquals(testAlbum, newTitle);
    }

    @Test
    public void addGenreTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(source);
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe();
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(source);
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre1 + "\u0000" + testGenre2, newGenres);
    }

    @Test
    public void removeGenreTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(source);
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe();
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe();
        sourceEditor.removeCompositionGenre(source, testGenre1).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(source);
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre2, newGenres);
    }

    @Test
    public void removeLastTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(source);
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe();
        sourceEditor.removeCompositionGenre(source, testGenre1).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(source);
        System.out.println("new genres: " + newGenres);
        assertEquals("", newGenres);
    }

    @Test
    public void changeGenreTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        String genres = sourceEditor.getCompositionGenre(source);
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        String testGenre3 = "Test genre3";
        sourceEditor.addCompositionGenre(source, testGenre1).subscribe();
        sourceEditor.addCompositionGenre(source, testGenre2).subscribe();
        sourceEditor.changeCompositionGenre(source, testGenre1, testGenre3).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(source);
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre3 + "\u0000" + testGenre2, newGenres);
    }

    @Test
    public void changeAlbumArtistTest() {
        when(musicProvider.getCompositionFilePath(anyLong())).thenReturn(filePath);
        System.out.println("album artist: " + sourceEditor.getCompositionAlbumArtist(source));

        String testName = "Test album artist";
        sourceEditor.setCompositionAlbumArtist(source, testName).subscribe();
        String newArtist = sourceEditor.getCompositionAlbumArtist(source);
        System.out.println("new album artist: " + newArtist);
        assertEquals(testName, newArtist);
    }

    @Test
    public void testFileWithWrongEncoding() {
        File file = new File("src/test/resources/Back In Black.mp3");
        when(contentSourceHelper.getAsFile(any())).thenReturn(file);

        System.out.println("title: " +  sourceEditor.getCompositionTitle(source));
        System.out.println("author: " + sourceEditor.getCompositionAuthor(source));
        System.out.println("album: " + sourceEditor.getCompositionAlbum(source));
        System.out.println("album artist: " + sourceEditor.getCompositionAlbumArtist(source));
    }
}