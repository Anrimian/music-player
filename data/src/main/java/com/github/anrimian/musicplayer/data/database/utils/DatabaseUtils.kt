package com.github.anrimian.musicplayer.data.database.utils

object DatabaseUtils {

    fun getSearchArg(arg: String?): String? {
        return arg?.let { "%$it%" }
    }

    fun getSearchArgs(arg: String?, count: Int): Array<String?> {
        val verifiedArg = if (arg.isNullOrEmpty()) null else arg
        return Array(count) { i ->
            if (i == 0) {
                verifiedArg
            } else {
                getSearchArg(verifiedArg)
            }
        }
    }

}
