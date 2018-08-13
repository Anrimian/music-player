package com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.folder;

import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import java.util.Comparator;
import java.util.Date;

public class CreateDateFileComparator implements Comparator<FileSource> {

    @Override
    public int compare(FileSource first, FileSource second) {
        if (first.getClass().equals(second.getClass())) {
            if (first instanceof FolderFileSource) {
                Date secondDate = ((FolderFileSource) second).getLatestCreateDate();
                if (secondDate == null) {
                    return -1;
                }
                Date firstDate = ((FolderFileSource) first).getLatestCreateDate();
                if (firstDate == null) {
                    return 1;
                }
                return firstDate.compareTo(secondDate);
            } else if (first instanceof MusicFileSource) {
                return ((MusicFileSource) first).getComposition().getDateAdded()
                        .compareTo(((MusicFileSource) second).getComposition().getDateAdded());
            }
        } else {
            return first instanceof FolderFileSource? -1 : 1;
        }
        return 0;
    }
}
