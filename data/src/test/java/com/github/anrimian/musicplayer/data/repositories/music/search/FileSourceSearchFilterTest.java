package com.github.anrimian.musicplayer.data.repositories.music.search;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;

import org.junit.Test;

import java.util.Date;

public class FileSourceSearchFilterTest {

    private FileSourceSearchFilter searchFilter = new FileSourceSearchFilter();

    @Test
    public void folderSearchFilterTest() {
        FileSource fileSource = new FolderFileSource("awersome/lol/kek",
                1, new Date(1), new Date(2));

        assert searchFilter.isSuitForSearch(fileSource, "kek");
        assert searchFilter.isSuitForSearch(fileSource, "KEK");
        assert !searchFilter.isSuitForSearch(fileSource, "/kek");
        assert !searchFilter.isSuitForSearch(fileSource, "some");
    }

    @Test
    public void compositionSearchFilterTest() {
        Composition composition = new Composition();
        composition.setArtist("marylin manson");
        composition.setDisplayName("kek.mp3");

        FileSource fileSource = new MusicFileSource(composition);
        assert searchFilter.isSuitForSearch(fileSource, "mary");
        assert searchFilter.isSuitForSearch(fileSource, "son");
        assert searchFilter.isSuitForSearch(fileSource, "SON");
        assert searchFilter.isSuitForSearch(fileSource, "ek");
        assert !searchFilter.isSuitForSearch(fileSource, "some");
        assert !searchFilter.isSuitForSearch(fileSource, "mp3");
    }
}