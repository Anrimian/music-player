package com.github.anrimian.musicplayer.ui.utils.compose

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

@Immutable
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(@param:StringRes val resId: Int, vararg val args: Any) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false

            if (resId != other.resId) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    class PluralResource(
        @param:PluralsRes val resId: Int,
        val quantity: Int,
        vararg val args: Any
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PluralResource) return false

            if (resId != other.resId) return false
            if (quantity != other.quantity) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + quantity
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    fun resolve(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
            is PluralResource -> context.resources.getQuantityString(resId, quantity, *args)
        }
    }

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
            is PluralResource -> pluralStringResource(resId, quantity, *args)
        }
    }

}

