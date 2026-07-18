package com.github.anrimian.musicplayer.ui.utils.compose

import kotlin.reflect.KProperty

class Ref<T>(var value: T)

operator fun <T> Ref<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
operator fun <T> Ref<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}
