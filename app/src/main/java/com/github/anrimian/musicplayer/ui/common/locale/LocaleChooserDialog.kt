package com.github.anrimian.musicplayer.ui.common.locale

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogMenuBinding
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter
import com.github.anrimian.musicplayer.ui.utils.views.menu.SimpleMenuBuilder
import java.util.Locale

private const val FOLLOW_SYSTEM_LANGUAGE_ID = -1

//highlight current locale
fun showLocaleChooserDialog(context: Context, onCompleteListener: (Locale?) -> Unit): Dialog {
    val binding = DialogMenuBinding.inflate(LayoutInflater.from(context))
    val view = binding.root

    binding.rvMenuItems.layoutManager = LinearLayoutManager(context)

    val menuBuilder = SimpleMenuBuilder(context)
    menuBuilder.add(FOLLOW_SYSTEM_LANGUAGE_ID, context.getString(R.string.follow_system_language))
    val locales = getAppLanguages(context, R.string.close, Locale.ENGLISH)
    locales.forEachIndexed { index, locale ->
        var title = locale.displayLanguage.replaceFirstChar { c -> c.uppercase() }

        val nativeName = locale.getDisplayLanguage(locale).replaceFirstChar { c -> c.uppercase() }
        if (!title.equals(nativeName, ignoreCase = true)) {
            title += "/$nativeName"
        }

        if (locale.language == "zh") {
            if (locale.country == "TW" || locale.country == "HK" || locale.script == "Hant") {
                title = "繁體中文 (Traditional)"
            } else {
                title = "简体中文 (Simplified)"
            }
        } else if (locale.country.isNotEmpty()) {
            title += " (${locale.displayCountry})"
        }

        menuBuilder.add(index, title)
    }

    val dialog = AlertDialog.Builder(context)
        .setTitle(R.string.language)
        .setView(view)
        .setNegativeButton(R.string.close) { _, _ -> }
        .show()

    val menuAdapter = MenuAdapter(menuBuilder.items, R.layout.item_dialog_menu) { menuItem ->
        val itemId = menuItem.itemId
        val locale = if (itemId == FOLLOW_SYSTEM_LANGUAGE_ID) {
            null
        } else {
            locales[menuItem.itemId]
        }
        onCompleteListener(locale)
        dialog.dismiss()
    }
    binding.rvMenuItems.adapter = menuAdapter
    return dialog
}

// default language case
// language - switch to system language - items remain in previous language
@Suppress("DEPRECATION")
private fun getAppLanguages(
    context: Context,
    @StringRes anyStringResId: Int,
    defaultLocale: Locale
): List<Locale> {
    val listAppLocales = ArrayList<Locale>()
    val addedLanguageTags = HashSet<String>()

    val metrics = DisplayMetrics()
    val res = context.resources
    val conf = res.configuration
    val localesStr = res.assets.locales

    for (localeStr in localesStr) {
        val tempLocale = parseLocaleString(localeStr)

        val normalizedLocale = when (tempLocale.language) {
            "zh" -> {
                val country = tempLocale.country
                if (country == "TW" || country == "HK" || country == "MO") {
                    Locale.TAIWAN
                } else {
                    Locale.CHINESE
                }
            }
            else -> {
                if (tempLocale.country.isNotEmpty()) {
                    Locale(tempLocale.language)
                } else {
                    tempLocale
                }
            }
        }

        val tag = normalizedLocale.toLanguageTag()
        if (addedLanguageTags.contains(tag)) {
            continue
        }

        conf.locale = normalizedLocale
        val res1 = Resources(context.assets, metrics, conf)
        val s1 = res1.getString(anyStringResId)

        conf.locale = Locale("") // default
        val res2 = Resources(context.assets, metrics, conf)
        val defaultString = res2.getString(anyStringResId)

        if (s1 != defaultString || normalizedLocale == defaultLocale) {
            listAppLocales.add(normalizedLocale)
            addedLanguageTags.add(tag)
        }
    }
    return listAppLocales
}

@Suppress("DEPRECATION")
private fun parseLocaleString(localeStr: String): Locale {
    return if (localeStr.contains("_")) {
        val parts = localeStr.split("_")
        if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
    } else if (localeStr.contains("-")) {
        val parts = localeStr.split("-")
        if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
    } else {
        Locale(localeStr)
    }
}