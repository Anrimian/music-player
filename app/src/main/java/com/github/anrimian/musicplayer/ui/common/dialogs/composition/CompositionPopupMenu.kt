package com.github.anrimian.musicplayer.ui.common.dialogs.composition

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.PartialCompositionMenuHeaderBinding
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder
import com.github.anrimian.musicplayer.ui.common.menu.AppPopupWindow
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SingleItemAdapter
import java.util.concurrent.atomic.AtomicReference

fun showCompositionPopupMenu(
    anchorView: View,
    @MenuRes menuResId: Int,
    composition: Composition,
    listener: (MenuItem) -> Unit,
) {
    val context = anchorView.context
    @SuppressLint("InflateParams")
    val popupView = LayoutInflater.from(context).inflate(R.layout.menu_popup, null)

    val headerItem = SingleItemAdapter { inflater, root ->
        PartialCompositionMenuHeaderBinding.inflate(inflater, root, false)
    }

    headerItem.runAction { binding ->
        binding.tvCompositionName.text = CompositionHelper.formatCompositionName(composition)

        val sb = DescriptionSpannableStringBuilder(context)
        sb.append(FormatUtils.formatCompositionAuthor(composition, context))
        sb.append(FormatUtils.formatMilliseconds(composition.duration))
        sb.append(FormatUtils.formatSize(context, composition.size))
        binding.tvCompositionInfo.text = sb
    }

    val recyclerView = popupView.findViewById<RecyclerView>(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(anchorView.context)

    val popupWindow = AtomicReference<PopupWindow>()

    val menu = AndroidUtils.createMenu(context, menuResId)
    val menuAdapter = MenuAdapter(menu, R.layout.item_popup_menu)
    menuAdapter.setOnItemClickListener { item ->
        listener(item)
        popupWindow.get()?.dismiss()
    }
    recyclerView.adapter = ConcatAdapter(headerItem, menuAdapter)

    val screenMargin = context.resources.getDimensionPixelSize(R.dimen.popup_screen_margin)
    popupWindow.set(AppPopupWindow.showPopupWindow(anchorView, popupView, Gravity.START, screenMargin))
}