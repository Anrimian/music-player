package com.github.anrimian.musicplayer.ui.common.error

/**
 * Created on 29.10.2017.
 */
open class ErrorCommand(val message: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ErrorCommand) return false

        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }

}