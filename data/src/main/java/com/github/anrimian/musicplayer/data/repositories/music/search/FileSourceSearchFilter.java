package com.github.anrimian.musicplayer.data.repositories.music.search;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.utils.search.SearchFilter;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.containsIgnoreCase;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.getLastPathSegment;

public class FileSourceSearchFilter implements SearchFilter<FileSource> {

    private CompositionSearchFilter compositionSearchFilter = new CompositionSearchFilter();

    @Override
    public boolean isSuitForSearch(@NonNull FileSource data, @NonNull String search) {
        if (data instanceof MusicFileSource) {
            Composition composition = ((MusicFileSource) data).getComposition();
            return compositionSearchFilter.isSuitForSearch(composition, search);
        }
        if (data instanceof FolderFileSource) {
            String path = ((FolderFileSource) data).getFullPath();
            path = getLastPathSegment(path);
            return containsIgnoreCase(path, search);
        }
        return false;
    }
}
