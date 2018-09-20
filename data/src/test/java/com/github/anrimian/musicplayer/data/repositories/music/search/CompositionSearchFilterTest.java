package com.github.anrimian.musicplayer.data.repositories.music.search;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import org.junit.Test;

import static org.junit.Assert.*;

public class CompositionSearchFilterTest {

    private CompositionSearchFilter searchFilter = new CompositionSearchFilter();

    @Test
    public void isSuitForSearchTest() {
        Composition composition = new Composition();
        composition.setArtist("marylin manson");
        composition.setDisplayName("kek.mp3");

        assert searchFilter.isSuitForSearch(composition, "mary");
        assert searchFilter.isSuitForSearch(composition, "son");
        assert searchFilter.isSuitForSearch(composition, "ek");
        assert !searchFilter.isSuitForSearch(composition, "some");
        assert !searchFilter.isSuitForSearch(composition, "mp3");
    }
}