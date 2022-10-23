package com.github.anrimian.filesync.models

class ProgressInfo(var current: Long = -1, var total: Long = -1) {

    fun set(current: Long, total: Long) {
        this.current = current
        this.total = total
    }

    override fun toString(): String {
        return "ProgressInfo(current=$current, total=$total)"
    }

}