package com.github.anrimian.simplemusicplayer.data.repositories.music.sort.folder;

import com.github.anrimian.simplemusicplayer.data.repositories.music.sort.Sorter;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import java.util.Collections;

public class CreateDateDescFolderSorter implements Sorter<Folder> {

    @Override
    public void applyOrder(Folder data) {
/*        Collections.sort(data.getFiles(), (first, second) -> {
            if (first.getClass().equals(second.getClass())) {
                if (first instanceof FolderFileSource) {
                    return ((FolderFileSource) second).getNewestCreateDate()
                            .compareTo(((FolderFileSource) first).getNewestCreateDate());
                } else if (first instanceof MusicFileSource) {
                    return ((MusicFileSource) second).getComposition().getDateAdded()
                            .compareTo(((MusicFileSource) first).getComposition().getDateAdded());
                }
            } else {
                return first instanceof FolderFileSource? -1 : 1;
            }
            return 0;
        });*/
    }
}
