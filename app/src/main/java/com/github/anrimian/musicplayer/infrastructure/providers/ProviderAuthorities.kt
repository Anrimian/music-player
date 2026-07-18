package com.github.anrimian.musicplayer.infrastructure.providers

import android.content.ComponentName
import android.content.ContentProvider
import android.content.Context
import java.util.concurrent.ConcurrentHashMap

object ProviderAuthorities {

    private val cache = ConcurrentHashMap<String, String>()

    @JvmStatic
    fun of(context: Context, cls: Class<out ContentProvider>): String =
        cache.getOrPut(cls.name) {
            context.packageManager
                .getProviderInfo(ComponentName(context, cls), 0)
                .authority
        }
}
