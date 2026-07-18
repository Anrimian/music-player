package com.github.anrimian.musicplayer.infrastructure.service.wearable

class WindowListData<T>(
    var startOffset: Int,
    var endOffset: Int,
    var list: List<T> = emptyList(),
    var updateTime: Long? = null
) {

    fun last() = list.last()
    fun first() = list.first()

}