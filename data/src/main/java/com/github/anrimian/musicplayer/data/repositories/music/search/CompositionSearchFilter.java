package com.github.anrimian.musicplayer.data.repositories.music.search;

import android.support.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.search.SearchFilter;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.containsIgnoreCase;

public class CompositionSearchFilter implements SearchFilter<Composition> {

    @Override
    public boolean isSuitForSearch(@NonNull Composition data, @NonNull String search) {
        String artist = data.getArtist();
        String displayName = data.getDisplayName();
        displayName = displayName.substring(0, displayName.lastIndexOf('.'));
        return containsIgnoreCase(displayName, search)
                || (artist != null && containsIgnoreCase(artist, search));
    }
}
