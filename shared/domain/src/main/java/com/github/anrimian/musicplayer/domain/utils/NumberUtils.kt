package com.github.anrimian.musicplayer.domain.utils

fun Long.millisToMinutes() = this/1000/60

fun Long.minutesToMillis() = this*60*1000L

fun boundValue(value: Float, start: Float, end: Float): Float {
    return if (value <= start) {
        0.0f
    } else if (value >= end) {
        1.0f
    } else {
        (value - start) / (end - start)
    }
}

object NumberUtils {

    fun booleanToBytes(value: Boolean, byteArray: ByteArray, start: Int = 0): ByteArray {
        byteArray[start] = if (value) 1 else 0
        return byteArray
    }

    fun bytesToBoolean(byteArray: ByteArray, start: Int = 0): Boolean {
        return byteArray[start] == 1.toByte()
    }

    fun floatToBytes(value: Float, byteArray: ByteArray, start: Int = 0): ByteArray {
        val bitRep = value.toRawBits()
        return intToBytes(bitRep, byteArray, start)
    }

    fun bytesToFloat(byteArray: ByteArray, start: Int = 0): Float {
        val bitRep = bytesToInt(byteArray, start)
        return Float.fromBits(bitRep)
    }

    fun longToBytes(value: Long, byteArray: ByteArray, start: Int = 0): ByteArray {
        var l = value
        for (i in start + Long.SIZE_BYTES - 1 downTo start) {
            byteArray[i] = (l and 0xFFL).toByte()
            l = l shr Byte.SIZE_BITS
        }
        return byteArray
    }

    fun bytesToLong(byteArray: ByteArray, start: Int = 0): Long {
        var result: Long = 0
        for (i in start until start + Long.SIZE_BYTES) {
            result = result shl Byte.SIZE_BITS
            result = result or (byteArray[i].toInt() and 0xFF).toLong()
        }
        return result
    }

    fun intToBytes(value: Int, byteArray: ByteArray, start: Int = 0): ByteArray {
        var l = value
        for (i in start + Int.SIZE_BYTES - 1 downTo start) {
            byteArray[i] = (l and 0xFF).toByte()
            l = l shr Byte.SIZE_BITS
        }
        return byteArray
    }

    fun bytesToInt(byteArray: ByteArray, start: Int = 0): Int {
        var result = 0
        for (i in start until start + Int.SIZE_BYTES) {
            result = result shl Byte.SIZE_BITS
            result = result or (byteArray[i].toInt() and 0xFF)
        }
        return result
    }

}