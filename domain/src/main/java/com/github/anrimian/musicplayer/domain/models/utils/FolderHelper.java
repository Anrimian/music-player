package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import javax.annotation.Nonnull;

public class FolderHelper {

    public static boolean areSourcesTheSame(FileSource oldSource, FileSource newSource) {
        if (oldSource.getClass().equals(newSource.getClass())) {
            if (oldSource instanceof FolderFileSource) {
                return !FolderHelper.hasChanges(((FolderFileSource) oldSource),
                        ((FolderFileSource) newSource));
            }
            if (oldSource instanceof MusicFileSource) {
                return CompositionHelper.areSourcesTheSame(((MusicFileSource) oldSource).getComposition(),
                        ((MusicFileSource) newSource).getComposition());
            }
        }
        return false;
    }

    public static boolean hasChanges(@Nonnull FolderFileSource first, @Nonnull FolderFileSource second) {
        return !Objects.equals(first.getEarliestCreateDate(), second.getEarliestCreateDate())
                || first.getFilesCount() != second.getFilesCount()
                || !Objects.equals(first.getFullPath(), second.getFullPath());
    }
}
