package com.github.anrimian.musicplayer.ui.utils.compose

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Partitions the collection into two [ImmutableList]s
 */
inline fun <T> Iterable<T>.partitionToImmutable(
    predicate: (T) -> Boolean
): Pair<ImmutableList<T>, ImmutableList<T>> {
    val first = persistentListOf<T>().builder()
    val second = persistentListOf<T>().builder()
    for (item in this) {
        if (predicate(item)) {
            first.add(item)
        } else {
            second.add(item)
        }
    }
    return first.build() to second.build()
}
