package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.List;

import javax.annotation.Nonnull;

public class CompositionHelper {

    public static boolean areSourcesTheSame(@Nonnull Composition first, @Nonnull Composition second) {
        return Objects.equals(first.getAlbum(), second.getAlbum())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getComposer(), second.getComposer())
                && Objects.equals(first.getDateAdded(), second.getDateAdded())
                && Objects.equals(first.getDateModified(), second.getDateModified())
                && Objects.equals(first.getDisplayName(), second.getDisplayName())
                && first.getDuration() == second.getDuration()
                && Objects.equals(first.getFilePath(), second.getFilePath())
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle())
                && Objects.equals(first.getYear(), second.getYear())
                && first.isCorrupted() == second.isCorrupted();
    }

    public static int getTotalDuration(List<Composition> compositions) {
        int totalDuration = 0;
        for (Composition composition: compositions) {
            totalDuration += composition.getDuration();
        }
        return totalDuration;
    }
}
