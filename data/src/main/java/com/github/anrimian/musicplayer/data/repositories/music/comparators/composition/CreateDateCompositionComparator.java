package com.github.anrimian.musicplayer.data.repositories.music.comparators.composition;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.text.Collator;
import java.util.Comparator;

public class CreateDateCompositionComparator implements Comparator<Composition> {

    @Override
    public int compare(Composition first, Composition second) {
        return first.getDateAdded().compareTo(second.getDateAdded());
    }
}
