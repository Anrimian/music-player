package com.github.anrimian.common

object AppWearUtils {

    fun buildEventPath(pathPrefix: String, eventName: String, requestId: Int?): String {
        val sb = StringBuilder()
        sb.append(pathPrefix)
        sb.append(eventName)
        if (requestId != null) {
            sb.append('-')
            sb.append(requestId)
        }
        return sb.toString()
    }

}