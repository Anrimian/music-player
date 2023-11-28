package com.github.anrimian.musicplayer.domain.utils

import java.text.Normalizer

fun normalize(s: String): String {
    return Normalizer.normalize(s, Normalizer.Form.NFD)//use NFKD? https://stackoverflow.com/a/285791/5541688
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}