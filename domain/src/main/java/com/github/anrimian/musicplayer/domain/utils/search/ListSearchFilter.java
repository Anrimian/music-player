package com.github.anrimian.musicplayer.domain.utils.search;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ListSearchFilter {

    public static <T, S> List<T> filterList(@Nonnull List<T> list,
                                            @Nullable S searchData,
                                            SearchFilter<T, S> searchFilter) {
        if (searchData == null) {
            return list;
        }

        List<T> filteredList = new ArrayList<>();
        for (T item: list) {
            if (searchFilter.isSuitForSearch(item, searchData)) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }
}
