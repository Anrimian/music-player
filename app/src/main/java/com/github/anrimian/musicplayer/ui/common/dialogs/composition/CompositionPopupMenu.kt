package com.github.anrimian.musicplayer.ui.common.dialogs.composition

import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.MenuPopupBinding
import com.github.anrimian.musicplayer.databinding.PartialCompositionMenuHeaderBinding
import com.github.anrimian.musicplayer.databinding.PartialPopupPagerSecondaryHeaderBinding
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder
import com.github.anrimian.musicplayer.ui.common.menu.AppPopupWindow
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuPagerWindow
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.setCurrentItem
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SingleItemAdapter
import java.util.concurrent.atomic.AtomicReference

fun showCompositionPopupMenu(
    anchorView: View,
    @MenuRes menuResId: Int,
    composition: Composition,
    listener: (MenuItem) -> Unit,
) {
    val context = anchorView.context
    val popupWindowRef = AtomicReference<PopupWindow>()
    val pagerRef = AtomicReference<ViewPager2>()

    val menu = AndroidUtils.createMenu(context, menuResId)
    val menuItems = AndroidUtils.getMenuItems(menu)
    val primaryItems = ArrayList<MenuItem>()
    val secondaryItems = ArrayList<MenuItem>()
    PopupMenuWindow.splitMenus(menuItems, primaryItems, secondaryItems)

    val headerItem = SingleItemAdapter { inflater, root ->
        PartialCompositionMenuHeaderBinding.inflate(inflater, root, false).also { binding ->
            if (secondaryItems.isNotEmpty()) {
                binding.root.setOnClickListener {
                    pagerRef.get().setCurrentItem(1, PopupMenuWindow.PAGER_SWIPE_ANIMATION_DURATION_MILLIS)
                }
            }
        }
    }
    headerItem.runAction { binding ->
        binding.tvCompositionName.text = CompositionHelper.formatCompositionName(composition)

        val sb = DescriptionSpannableStringBuilder(context)
        sb.append(FormatUtils.formatCompositionAuthor(composition, context))
        sb.append(FormatUtils.formatMilliseconds(composition.duration))
        sb.append(FormatUtils.formatSize(context, composition.size))
        binding.tvCompositionInfo.text = sb
    }

    val gravity = Gravity.START
    val screenMargin = context.resources.getDimensionPixelSize(R.dimen.popup_screen_margin)
    if (secondaryItems.isEmpty()) {
        val menuBinding = MenuPopupBinding.inflate(LayoutInflater.from(context))

        menuBinding.rvMenuItems.layoutManager = LinearLayoutManager(context)
        val menuAdapter = PopupMenuWindow.createMenuAdapter(menuItems, listener, popupWindowRef)
        menuBinding.rvMenuItems.adapter = ConcatAdapter(headerItem, menuAdapter)

        popupWindowRef.set(AppPopupWindow.showPopupWindow(anchorView, menuBinding.root, gravity, screenMargin))
    } else {
        val secondaryMenuHeaderItem = SingleItemAdapter { inflater, parent ->
            PartialPopupPagerSecondaryHeaderBinding.inflate(inflater, parent, false).apply {
                llBack.setOnClickListener { pagerRef.get().setCurrentItem(0, PopupMenuWindow.PAGER_SWIPE_ANIMATION_DURATION_MILLIS) }
            }
        }
        PopupMenuPagerWindow.showPagerPopup(
            anchorView,
            ConcatAdapter(headerItem, PopupMenuWindow.createMenuAdapter(primaryItems, listener, popupWindowRef)),
            ConcatAdapter(secondaryMenuHeaderItem, PopupMenuWindow.createMenuAdapter(secondaryItems, listener, popupWindowRef)),
            popupWindowRef,
            pagerRef,
            gravity,
            screenMargin
        )
    }

}