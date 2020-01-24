package com.github.anrimian.musicplayer.data.repositories.library.comparators.folder;

import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;

import java.util.Comparator;
import java.util.Date;

public class CreateDateFileComparator implements Comparator<FileSource> {

    @Override
    public int compare(FileSource first, FileSource second) {
        if (first instanceof FolderFileSource) {
            Date secondDate = ((FolderFileSource) second).getLatestCreateDate();
            Date firstDate = ((FolderFileSource) first).getLatestCreateDate();
            if (firstDate == null || secondDate == null) {
                return 1;
            }
            return firstDate.compareTo(secondDate);
        } else if (first instanceof MusicFileSource) {
            return ((MusicFileSource) first).getComposition().getDateAdded()
                    .compareTo(((MusicFileSource) second).getComposition().getDateAdded());
        }
        return 0;
    }
}
