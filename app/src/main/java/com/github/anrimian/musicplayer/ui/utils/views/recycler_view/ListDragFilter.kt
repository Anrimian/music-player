package com.github.anrimian.musicplayer.ui.utils.views.recycler_view

private const val MAX_EVENTS_TO_SKIP: Short = 50

class ListDragFilter {

    private var currentSize = 0
    private var eventsToSkip: Short = 0

    fun increaseEventsToSkip() {
        eventsToSkip++.coerceAtMost(MAX_EVENTS_TO_SKIP)
    }

    fun filterListEmitting(list: List<Any>): Boolean {
        if (currentSize != list.size) {
            currentSize = list.size
            return true
        }

        val isAllowed = eventsToSkip <= 0
        if (eventsToSkip > 0) {
            eventsToSkip--
        }
        return isAllowed
    }
}