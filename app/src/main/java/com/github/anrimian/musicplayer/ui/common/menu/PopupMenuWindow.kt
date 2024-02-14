package com.github.anrimian.musicplayer.ui.common.menu

import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.MenuPopupBinding
import com.github.anrimian.musicplayer.databinding.PartialPopupPagerSecondaryHeaderBinding
import com.github.anrimian.musicplayer.domain.utils.functions.Callback
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter
import com.github.anrimian.musicplayer.ui.utils.setCurrentItem
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SingleItemAdapter
import java.util.concurrent.atomic.AtomicReference

object PopupMenuWindow {

    const val PAGER_SWIPE_ANIMATION_DURATION_MILLIS = 250L

    fun showPopup(
        anchorView: View,
        @MenuRes menuResId: Int,
        gravity: Int = Gravity.START,
        listener: Callback<MenuItem>,
    ) {
        val menu = AndroidUtils.createMenu(anchorView.context, menuResId)
        showPopup(anchorView, menu, gravity, listener)
    }

    fun showPopup(
        anchorView: View,
        menu: Menu,
        gravity: Int = Gravity.START,
        listener: Callback<MenuItem>,
    ) {
        showPopup(anchorView, AndroidUtils.getMenuItems(menu), gravity, listener)
    }

    fun showPopup(
        anchorView: View,
        menuItems: List<MenuItem>,
        gravity: Int = Gravity.START,
        listener: Callback<MenuItem>,
    ) {
        val screenMargin = anchorView.resources.getDimensionPixelSize(R.dimen.popup_screen_margin)
        showPopup(anchorView, menuItems, listener, gravity, screenMargin)
    }

    @JvmStatic
    @JvmOverloads
    fun showActionBarPopup(
        anchorView: View,
        items: ArrayList<MenuItemImpl>,
        listener: Callback<MenuItem>,
        gravity: Int = Gravity.CENTER,
    ) {
        val screenMargin = anchorView.resources.getDimensionPixelSize(R.dimen.action_bar_popup_screen_margin)
        showPopup(anchorView, items, listener, gravity, screenMargin)
    }

    fun createMenuAdapter(
        menuItems: List<MenuItem>,
        listener: Callback<MenuItem>,
        popupWindowRef: AtomicReference<PopupWindow>,
    ): MenuAdapter {
        val menuAdapter = MenuAdapter(menuItems, R.layout.item_popup_menu)
        menuAdapter.setOnItemClickListener { item ->
            listener.call(item)
            val popup = popupWindowRef.get()
            popup?.dismiss()
        }
        return menuAdapter
    }

    fun splitMenus(
        menuItems: List<MenuItem>,
        outPrimaryItems: ArrayList<MenuItem>,
        outSecondaryItems: ArrayList<MenuItem>
    ) {
        for (menuItem in menuItems) {
            if (!menuItem.isVisible) {
                continue
            }
            if (menuItem.groupId == 0) {
                outPrimaryItems.add(menuItem)
            } else {
                outSecondaryItems.add(menuItem)
            }
        }
    }

    private fun showPopup(
        anchorView: View,
        menuItems: List<MenuItem>,
        listener: Callback<MenuItem>,
        gravity: Int,
        screenMargin: Int,
    ) {
        val primaryItems = ArrayList<MenuItem>()
        val secondaryItems = ArrayList<MenuItem>()
        splitMenus(menuItems, primaryItems, secondaryItems)
        if (secondaryItems.isEmpty()) {
            showSimplePopup(anchorView, menuItems, listener, gravity, screenMargin)
        } else {
            showPagerPopup(anchorView, primaryItems, secondaryItems, listener, gravity, screenMargin)
        }
    }

    private fun showSimplePopup(
        anchorView: View,
        menuItems: List<MenuItem>,
        listener: Callback<MenuItem>,
        gravity: Int,
        screenMargin: Int,
    ) {
        val context = anchorView.context
        val popupWindowRef = AtomicReference<PopupWindow>()
        val menuBinding = MenuPopupBinding.inflate(LayoutInflater.from(context))

        menuBinding.rvMenuItems.layoutManager = LinearLayoutManager(context)
        menuBinding.rvMenuItems.adapter = createMenuAdapter(menuItems, listener, popupWindowRef)

        popupWindowRef.set(
            AppPopupWindow.showPopupWindow(anchorView, menuBinding.root, gravity, screenMargin)
        )
    }

    private fun showPagerPopup(
        anchorView: View,
        primaryItems: List<MenuItem>,
        secondaryItems: List<MenuItem>,
        listener: Callback<MenuItem>,
        gravity: Int,
        screenMargin: Int,
    ) {
        val popupWindowRef = AtomicReference<PopupWindow>()
        val pagerRef = AtomicReference<ViewPager2>()

        val secondaryMenuHeaderItem = SingleItemAdapter { inflater, parent ->
            PartialPopupPagerSecondaryHeaderBinding.inflate(inflater, parent, false).apply {
                llBack.setOnClickListener { pagerRef.get().setCurrentItem(0, PAGER_SWIPE_ANIMATION_DURATION_MILLIS) }
            }
        }

        PopupMenuPagerWindow.showPagerPopup(
            anchorView,
            createMenuAdapter(primaryItems, listener, popupWindowRef),
            ConcatAdapter(secondaryMenuHeaderItem, createMenuAdapter(secondaryItems, listener, popupWindowRef)),
            popupWindowRef,
            pagerRef,
            gravity,
            screenMargin
        )
    }

}