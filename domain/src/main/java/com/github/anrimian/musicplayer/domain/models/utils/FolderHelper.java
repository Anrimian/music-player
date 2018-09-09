package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import javax.annotation.Nonnull;

public class FolderHelper {

    public static boolean hasChanges(@Nonnull FolderFileSource first, @Nonnull FolderFileSource second) {
        return !Objects.equals(first.getEarliestCreateDate(), second.getEarliestCreateDate())
                || first.getFilesCount() != second.getFilesCount()
                || !Objects.equals(first.getFullPath(), second.getFullPath());
    }
}
