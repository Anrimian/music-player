package com.github.anrimian.fsync.models.task

abstract class Task<K>(open val id: Long, open val storageId: Long) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Task<*>) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}