package com.github.anrimian.musicplayer.domain.utils.search;

import com.github.anrimian.musicplayer.domain.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ListSearchFilter {

    public static <T> List<T> filterList(@Nonnull List<T> list,
                                         @Nullable String searchData,
                                         SearchFilter<T> searchFilter) {
        if (TextUtils.isEmpty(searchData)) {
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
