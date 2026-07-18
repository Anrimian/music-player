package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry;

import java.util.List;

import javax.annotation.Nonnull;

public class PlayListItemHelper {

    public static boolean areSourcesTheSame(@Nonnull PlaylistEntry first,
                                            @Nonnull PlaylistEntry second) {
        return CompositionHelper.areSourcesTheSame(first, second);
    }

    public static List<Object> getChangePayload(PlaylistEntry first, PlaylistEntry second) {
        return CompositionHelper.getChangePayload(first, second);
    }
}
