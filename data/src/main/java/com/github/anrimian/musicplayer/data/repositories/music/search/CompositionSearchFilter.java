package com.github.anrimian.musicplayer.data.repositories.music.search;

import android.support.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.search.SearchFilter;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class CompositionSearchFilter implements SearchFilter<Composition, String> {

    @Override
    public boolean isSuitForSearch(@NonNull Composition data, @NonNull String search) {
        String artist = data.getArtist();
        String displayName = data.getDisplayName();
        displayName = displayName.substring(0, displayName.lastIndexOf('.'));
        return displayName.contains(search)
                || (artist != null && artist.contains(search));
    }
}
