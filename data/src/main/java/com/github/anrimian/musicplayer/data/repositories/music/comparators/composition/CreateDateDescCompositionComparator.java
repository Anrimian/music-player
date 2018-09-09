package com.github.anrimian.musicplayer.data.repositories.music.comparators.composition;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.Comparator;

public class CreateDateDescCompositionComparator implements Comparator<Composition> {

    @Override
    public int compare(Composition first, Composition second) {
        return second.getDateAdded().compareTo(first.getDateAdded());
    }
}
