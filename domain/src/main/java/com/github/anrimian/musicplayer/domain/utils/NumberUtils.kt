package com.github.anrimian.musicplayer.domain.utils

fun Long.millisToMinutes() = this/1000/60

fun Long.minutesToMillis() = this*60*1000L