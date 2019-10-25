package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.List;

import javax.annotation.Nonnull;

public class PlayListItemHelper {

    public static boolean areSourcesTheSame(@Nonnull PlayListItem first,
                                            @Nonnull PlayListItem second) {
        return CompositionHelper.areSourcesTheSame(first.getComposition(), second.getComposition());
    }

    public static List<Object> getChangePayload(PlayListItem first, PlayListItem second) {
        return CompositionHelper.getChangePayload(first.getComposition(), second.getComposition());
    }
}
