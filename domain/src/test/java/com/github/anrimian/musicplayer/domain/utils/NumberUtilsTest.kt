package com.github.anrimian.musicplayer.domain.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NumberUtilsTest {

    @Test
    fun `float test`() {
        val value = 5f
        val arr = ByteArray(Float.SIZE_BYTES)
        NumberUtils.floatToBytes(value, arr)
        val result = NumberUtils.bytesToFloat(arr)
        assertEquals(value, result)
    }


}