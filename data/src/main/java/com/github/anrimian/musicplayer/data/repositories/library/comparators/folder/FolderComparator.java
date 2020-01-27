package com.github.anrimian.musicplayer.data.repositories.library.comparators.folder;

import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;

import java.util.Comparator;

public class FolderComparator implements Comparator<FileSource> {

    private final Comparator<FileSource> comparator;

    public FolderComparator(Comparator<FileSource> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(FileSource first, FileSource second) {
        if (first.getClass().equals(second.getClass())) {
            return comparator.compare(first, second);
        }
        return first instanceof FolderFileSource? -1 : 1;
    }
}
