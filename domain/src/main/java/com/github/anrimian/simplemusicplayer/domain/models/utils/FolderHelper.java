package com.github.anrimian.simplemusicplayer.domain.models.utils;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.utils.Objects;

import javax.annotation.Nonnull;

public class FolderHelper {

    public static boolean hasChanges(@Nonnull FolderFileSource first, @Nonnull FolderFileSource second) {
        return !Objects.equals(first.getEarliestCreateDate(), second.getEarliestCreateDate())
                || first.getFilesCount() != second.getFilesCount()
                || !Objects.equals(first.getFullPath(), second.getFullPath());
    }
}
