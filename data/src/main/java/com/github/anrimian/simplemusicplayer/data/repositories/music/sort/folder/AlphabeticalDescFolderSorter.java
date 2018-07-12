package com.github.anrimian.simplemusicplayer.data.repositories.music.sort.folder;

import com.github.anrimian.simplemusicplayer.data.repositories.music.sort.Sorter;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import java.text.Collator;
import java.util.Collections;

public class AlphabeticalDescFolderSorter implements Sorter<Folder> {

    @Override
    public void applyOrder(Folder data) {
        Collections.sort(data.getFiles(), (first, second) -> {
            if (first.getClass().equals(second.getClass())) {
                Collator collator = Collator.getInstance();
                if (first instanceof FolderFileSource) {
                    return collator.compare(
                            ((FolderFileSource) second).getFullPath(),
                            ((FolderFileSource) first).getFullPath()
                    );
                } else if (first instanceof MusicFileSource) {
                    return collator.compare(
                            ((MusicFileSource) second).getComposition().getFilePath(),
                            ((MusicFileSource) first).getComposition().getFilePath()
                    );
                }
            } else {
                return first instanceof FolderFileSource? -1 : 1;
            }
            return 0;
        });
    }
}
