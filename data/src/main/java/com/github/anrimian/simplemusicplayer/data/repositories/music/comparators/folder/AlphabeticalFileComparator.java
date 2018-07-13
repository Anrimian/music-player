package com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.folder;

import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import java.text.Collator;
import java.util.Comparator;

public class AlphabeticalFileComparator implements Comparator<FileSource> {

    @Override
    public int compare(FileSource first, FileSource second) {
        if (first.getClass().equals(second.getClass())) {
            Collator collator = Collator.getInstance();
            if (first instanceof FolderFileSource) {
                return collator.compare(
                        ((FolderFileSource) first).getFullPath(),
                        ((FolderFileSource) second).getFullPath()
                );
            } else if (first instanceof MusicFileSource) {
                return collator.compare(
                        ((MusicFileSource) first).getComposition().getFilePath(),
                        ((MusicFileSource) second).getComposition().getFilePath()
                );
            }
        } else {
            return first instanceof FolderFileSource? -1 : 1;
        }
        return 0;
    }
}
