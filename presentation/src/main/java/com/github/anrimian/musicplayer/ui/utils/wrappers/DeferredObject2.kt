package com.github.anrimian.musicplayer.ui.utils.wrappers

import java.util.*

class DeferredObject2<T> {
    private var obj: T? = null
    private val deferredFunctions = LinkedList<(T) -> Unit>()

    fun setObject(obj: T) {
        this.obj = obj
        while (!deferredFunctions.isEmpty()) {
            deferredFunctions.pollFirst()!!.invoke(obj)
        }
    }

    fun call(function: (T) -> Unit) {
        if (obj != null) {
            function(obj!!)
        } else {
            deferredFunctions.add(function)
        }
    }
}