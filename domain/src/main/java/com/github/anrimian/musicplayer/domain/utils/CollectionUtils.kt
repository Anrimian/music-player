package com.github.anrimian.musicplayer.domain.utils

fun IntArray.indexOfOr(element: Int, defaultValue: Int): Int {
    val index = indexOf(element)
    if (index == -1) {
        return defaultValue
    }
    return index
}