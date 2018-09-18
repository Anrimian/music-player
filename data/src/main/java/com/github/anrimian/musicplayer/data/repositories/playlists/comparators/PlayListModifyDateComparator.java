package com.github.anrimian.musicplayer.data.repositories.playlists.comparators;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.util.Comparator;

public class PlayListModifyDateComparator implements Comparator<PlayList> {

    @Override
    public int compare(PlayList o1, PlayList o2) {
        return o2.getDateModified().compareTo(o1.getDateModified());
    }
}
