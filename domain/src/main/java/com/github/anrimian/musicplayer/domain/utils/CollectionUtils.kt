package com.github.anrimian.musicplayer.domain.utils

fun IntArray.indexOfOr(element: Int, defaultValue: Int): Int {
    val index = indexOf(element)
    if (index == -1) {
        return defaultValue
    }
    return index
}

inline fun <I> Collection<I>.toLongArray(mapper: (I) -> Long): LongArray {
    val ids = LongArray(size)
    forEachIndexed { index, value -> ids[index] = mapper(value) }
    return ids
}

fun <K, V1, V2> mergeMaps(
    firstMap: Map<K, V1>,
    secondMap: Map<K, V2>,
    onSecondEntryAdded: (V1) -> Unit,
    onFirstEntryAdded: (V2) -> Unit,
    hasChanges: (V1, V2) -> Boolean,
    isFirstEntryMoreActual: (V1, V2) -> Boolean,
    onFirstEntryModified: (old: V1, new: V2) -> Unit,
    onSecondEntryModified: (old: V2, new: V1) -> Unit,
) {
    for(firstEntry in firstMap) {
        val firstValue = firstEntry.value
        val secondValue = secondMap[firstEntry.key]
        if (secondValue != null) {
            if (!hasChanges(firstValue, secondValue)) {
                continue
            }
            if (isFirstEntryMoreActual(firstValue, secondValue)) {
                onSecondEntryModified(secondValue, firstValue)
            } else {
                onFirstEntryModified(firstValue, secondValue)
            }
        } else {
            onSecondEntryAdded(firstValue)
        }
    }
    for(secondEntry in secondMap) {
        if (!firstMap.containsKey(secondEntry.key)) {
            onFirstEntryAdded(secondEntry.value)
        }
    }
}

inline fun <K, V> MutableMap<K, V>.getOrPut(key: K, defaultValue: () -> V?): V? {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        if (answer != null) {
            put(key, answer)
        }
        answer
    } else {
        value
    }
}