package com.github.anrimian.musicplayer.ui.common.menu

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.MenuPopupPagerBinding
import com.github.anrimian.musicplayer.databinding.PartialFrameLayoutBinding
import com.github.anrimian.musicplayer.databinding.PartialPopupPagerListBinding
import com.github.anrimian.musicplayer.domain.utils.boundValue
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.reduceDragSensitivityBy
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SingleItemAdapter
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.min


object PopupMenuPagerWindow {

    fun showPagerPopup(
        anchorView: View,
        primaryListAdapter: RecyclerView.Adapter<*>,
        secondaryListAdapter: RecyclerView.Adapter<*>,
        popupWindowRef: AtomicReference<PopupWindow>,
        pagerRef: AtomicReference<ViewPager2>,
        gravity: Int,
        screenMargin: Int,
    ) {
        val context = anchorView.context
        val menuBinding = MenuPopupPagerBinding.inflate(LayoutInflater.from(context))
        pagerRef.set(menuBinding.menuPager)

        //primary page
        val rvFirst = createPagerRecyclerView(context, primaryListAdapter)
        val firstMenuWidth = rvFirst.measuredWidth
        val firstMenuHeight = rvFirst.measuredHeight
        val firstItemForeground = ColorDrawable(context.colorFromAttr(R.attr.popupPageForegroundShadowColor))
        val mainMenuItem = SingleItemAdapter { inflater, parent ->
            PartialFrameLayoutBinding.inflate(inflater, parent, false).apply {
                root.background = firstItemForeground
                root.addView(rvFirst, firstMenuWidth, firstMenuHeight)
            }
        }

        //secondary page
        val rvSecond = createPagerRecyclerView(context, secondaryListAdapter)
        val secondMenuWidth = rvSecond.measuredWidth
        val secondMenuHeight = rvSecond.measuredHeight
        val secondMenuItem = SingleItemAdapter { inflater, parent ->
            PartialFrameLayoutBinding.inflate(inflater, parent, false).apply {
                root.addView(rvSecond, secondMenuWidth, secondMenuHeight)
            }
        }


        menuBinding.menuPager.adapter = ConcatAdapter(mainMenuItem, secondMenuItem)

        val minWidth = min(firstMenuWidth, secondMenuWidth)
        val minHeight = min(firstMenuHeight, secondMenuHeight)
        val widthDiff = abs(firstMenuWidth - secondMenuWidth)
        val heightDiff = abs(firstMenuHeight - secondMenuHeight)
        menuBinding.menuPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                val offset = if (position == 0) 1 - positionOffset else positionOffset
                val bOffset = boundValue(offset, 0.05f, 0.85f)

                val bOffsetForeground = boundValue(1 - offset, 0.15f, 0.5f)
                firstItemForeground.alpha = (255 * (bOffsetForeground)).toInt()

                menuBinding.menuPager.updateLayoutParams<FrameLayout.LayoutParams> {
                    val widthOffset = if (minWidth == firstMenuWidth) 1 - bOffset else bOffset
                    val heightOffset = if (minHeight == firstMenuHeight) 1 - bOffset else bOffset
                    width = (minWidth + widthDiff * widthOffset).toInt()
                    height = (minHeight + heightDiff * heightOffset).toInt()
                }
            }

        })

        menuBinding.menuPager.reduceDragSensitivityBy(2)

        menuBinding.menuPager.updateLayoutParams<FrameLayout.LayoutParams> {
            width = firstMenuWidth
            height = firstMenuHeight
        }

        popupWindowRef.set(
            AppPopupWindow.showPopupWindow(anchorView, menuBinding.root, gravity, screenMargin)
        )
    }

    private fun createPagerRecyclerView(
        context: Context,
        adapter: RecyclerView.Adapter<*>,
    ): RecyclerView {
        val binding = PartialPopupPagerListBinding.inflate(LayoutInflater.from(context))
        val recyclerView = binding.rvMenuItems
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return recyclerView
    }

}