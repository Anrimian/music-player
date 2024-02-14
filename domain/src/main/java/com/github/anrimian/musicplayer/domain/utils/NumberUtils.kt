package com.github.anrimian.musicplayer.domain.utils

fun Long.millisToMinutes() = this/1000/60

fun Long.minutesToMillis() = this*60*1000L

fun boundValue(value: Float, start: Float, end: Float): Float {
    return if (value <= start) {
        0.0f
    } else if (value >= end) {
        1.0f
    } else {
        (value - start) / (end - start)
    }
}