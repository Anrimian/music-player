package com.github.anrimian.musicplayer.domain.models.common

class TimedChange<T1, T2>(val oldData: T1, val newData: T2, val time: Long)