package com.github.anrimian.musicplayer.data.repositories.music.comparators.folder;

import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;

import java.text.Collator;
import java.util.Comparator;

public class AlphabeticalFileComparator implements Comparator<FileSource> {

    @Override
    public int compare(FileSource first, FileSource second) {
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
        return 0;
    }
}
