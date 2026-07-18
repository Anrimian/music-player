package com.github.anrimian.musicplayer.data.utils

import org.json.JSONObject

fun JSONObject.optionalLong(name: String): Long? {
    return toLong(opt(name))
}

private fun toLong(value: Any?): Long? {
    when (value) {
        is Long -> return value
        is Number -> return value.toLong()
        is String -> try {
            return value.toDouble().toLong()
        } catch (ignored: NumberFormatException) {
        }
    }
    return null
}