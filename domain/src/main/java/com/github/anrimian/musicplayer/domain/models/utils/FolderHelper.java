package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.composition.folders.CompositionFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.Payloads.FILES_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

public class FolderHelper {

    public static boolean areSourcesTheSame(FileSource2 oldSource, FileSource2 newSource) {
        if (oldSource.getClass().equals(newSource.getClass())) {
            if (oldSource instanceof FolderFileSource2) {
                return !FolderHelper.hasChanges(((FolderFileSource2) oldSource),
                        ((FolderFileSource2) newSource));
            }
            if (oldSource instanceof CompositionFileSource2) {
                return CompositionHelper.areSourcesTheSame(((CompositionFileSource2) oldSource).getComposition(),
                        ((CompositionFileSource2) newSource).getComposition());
            }
        }
        return false;
    }

    public static List<Object> getChangePayload(FileSource2 oldSource, FileSource2 newSource) {
        if (oldSource.getClass().equals(newSource.getClass())) {
            if (oldSource instanceof FolderFileSource2) {
                return FolderHelper.getChangePayload(((FolderFileSource2) oldSource),
                        ((FolderFileSource2) newSource));
            }
            if (oldSource instanceof CompositionFileSource2) {
                return CompositionHelper.getChangePayload(
                        ((CompositionFileSource2) oldSource).getComposition(),
                        ((CompositionFileSource2) newSource).getComposition());
            }
        }
        return null;
    }

    public static boolean hasChanges(@Nonnull FolderFileSource2 first, @Nonnull FolderFileSource2 second) {
        return /*!Objects.equals(first.getEarliestCreateDate(), second.getEarliestCreateDate())
                || */first.getFilesCount() != second.getFilesCount()
                || !Objects.equals(first.getName(), second.getName());
    }

    private static List<Object> getChangePayload(FolderFileSource2 first, FolderFileSource2 second) {
        List<Object> payloads = new LinkedList<>();
//        if (!Objects.equals(first.getEarliestCreateDate(), second.getEarliestCreateDate())) {
//            payloads.add(CREATE_DATE);
//        }
        if (first.getFilesCount() != second.getFilesCount()) {
            payloads.add(FILES_COUNT);
        }
        if (!Objects.equals(first.getName(), second.getName())) {
            payloads.add(NAME);
        }
        return payloads;
    }
}
