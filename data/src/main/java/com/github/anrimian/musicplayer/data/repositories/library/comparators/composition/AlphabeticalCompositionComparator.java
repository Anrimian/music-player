package com.github.anrimian.musicplayer.data.repositories.library.comparators.composition;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.text.Collator;
import java.util.Comparator;

public class AlphabeticalCompositionComparator implements Comparator<Composition> {

    @Override
    public int compare(Composition first, Composition second) {
        return Collator.getInstance().compare(first.getFilePath(), second.getFilePath());
    }
}
