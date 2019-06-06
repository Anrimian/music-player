package com.github.anrimian.musicplayer.data.repositories.music.search;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.search.SearchFilter;

import androidx.annotation.NonNull;

import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.containsIgnoreCase;

public class CompositionSearchFilter implements SearchFilter<Composition> {

    @Override
    public boolean isSuitForSearch(@NonNull Composition data, @NonNull String search) {
        String artist = data.getArtist();
        String displayName = formatCompositionName(data);
        return containsIgnoreCase(displayName, search)
                || (artist != null && containsIgnoreCase(artist, search));
    }
}
