package com.github.anrimian.musicplayer.data.repositories.library.search;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import org.junit.Test;

import java.util.Date;

public class CompositionSearchFilterTest {

    private CompositionSearchFilter searchFilter = new CompositionSearchFilter();

    @Test
    public void isSuitForSearchTest() {
        Composition composition = new Composition("marylin manson",
                "kek",
                null,
                "kek.mp3",
                0,
                0,
                0,
                1L,
                new Date(0),
                new Date(0),
                null);

        assert searchFilter.isSuitForSearch(composition, "mary");
        assert searchFilter.isSuitForSearch(composition, "son");
        assert searchFilter.isSuitForSearch(composition, "SON");
        assert searchFilter.isSuitForSearch(composition, "ek");
        assert !searchFilter.isSuitForSearch(composition, "some");
        assert !searchFilter.isSuitForSearch(composition, "mp3");
    }
}