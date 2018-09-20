package com.github.anrimian.musicplayer.domain.utils.search;

import javax.annotation.Nonnull;

public interface SearchFilter<T, S> {

    boolean isSuitForSearch(@Nonnull T data, @Nonnull S search);
}
