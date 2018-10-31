package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.List;

import javax.annotation.Nonnull;

public class PlayListItemHelper {

    public static int getTotalDuration(List<PlayListItem> items) {
        int totalDuration = 0;
        for (PlayListItem item: items) {
            totalDuration += item.getComposition().getDuration();
        }
        return totalDuration;
    }

    public static boolean areSourcesTheSame(@Nonnull PlayListItem first,
                                            @Nonnull PlayListItem second) {
        return CompositionHelper.areSourcesTheSame(first.getComposition(), second.getComposition());
    }
}
