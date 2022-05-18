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
import java.util.*

private const val FOLLOW_SYSTEM_LANGUAGE_ID = -1

//highlight current locale
fun showLocaleChooserDialog(context: Context, onCompleteListener: (Locale?) -> Unit): Dialog {
    val binding = DialogMenuBinding.inflate(LayoutInflater.from(context))
    val view = binding.root

    binding.recyclerView.layoutManager = LinearLayoutManager(context)

    val menuBuilder = SimpleMenuBuilder(context)
    menuBuilder.add(FOLLOW_SYSTEM_LANGUAGE_ID, context.getString(R.string.follow_system_language))
    val locales = getAppLanguages(context, R.string.close, Locale.ENGLISH)
    locales.forEachIndexed { index, locale ->
        val title = locale.displayLanguage + '/' + locale.getDisplayLanguage(locale)
        menuBuilder.add(index, title)
    }

    val dialog = AlertDialog.Builder(context)
        .setTitle(R.string.language)
        .setView(view)
        .setNegativeButton(R.string.close) { _, _ -> }
        .show()

    val menuAdapter = MenuAdapter(menuBuilder.items, R.layout.item_dialog_menu)
    menuAdapter.setOnItemClickListener { menuItem ->
        val itemId = menuItem.itemId
        val locale = if (itemId == FOLLOW_SYSTEM_LANGUAGE_ID) {
            null
        } else {
            locales[menuItem.itemId]
        }
        onCompleteListener(locale)
        dialog.dismiss()
    }
    binding.recyclerView.adapter = menuAdapter
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

    val metrics = DisplayMetrics()
    val res = context.resources
    val conf = res.configuration
    val locales = res.assets.locales
    for (locale in locales) {
        val currentLocale = Locale(locale)
        conf.locale = currentLocale
        val res1 = Resources(context.assets, metrics, conf)
        val s1 = res1.getString(anyStringResId)

        conf.locale = Locale("")
        val res2 = Resources(context.assets, metrics, conf)
        val defaultString = res2.getString(anyStringResId)

        if (s1 != defaultString || currentLocale == defaultLocale) {
            listAppLocales.add(currentLocale)
        }
    }
    return listAppLocales
}