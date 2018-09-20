package com.github.anrimian.musicplayer.domain.utils.search;

import javax.annotation.Nonnull;

public interface SearchFilter<T> {

    boolean isSuitForSearch(@Nonnull T data, @Nonnull String search);
}
