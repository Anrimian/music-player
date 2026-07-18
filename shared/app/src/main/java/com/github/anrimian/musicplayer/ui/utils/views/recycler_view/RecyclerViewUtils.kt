package com.github.anrimian.musicplayer.ui.utils.views.recycler_view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller

fun scrollToPosition(
    rv: RecyclerView,
    lm: LinearLayoutManager,
    position: Int,
    smooth: Boolean,
) {
    val context = rv.context
    rv.post{
        if (smooth) {
            smoothScrollToTop(position, lm, context, 170)
        } else {
            lm.scrollToPositionWithOffset(position, 0)
        }
    }
}

fun smoothScrollToTop(
    position: Int,
    layoutManager: RecyclerView.LayoutManager,
    context: Context,
    duration: Int,
) {
    val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun calculateTimeForScrolling(dx: Int): Int {
            return duration
        }
    }
    smoothScroller.targetPosition = position
    layoutManager.startSmoothScroll(smoothScroller)
}

fun windowedScrollToPosition(
    context: Context,
    layoutManager: LinearLayoutManager,
    adapter: RecyclerView.Adapter<*>,
    position: Int,
    firstVisiblePosition: Int,
    lastVisiblePosition: Int,
    scrollWindowTopOffset: Int,
    scrollWindowBottomOffset: Int,
) {
    val scrollWindowBottomPositionOffset = (lastVisiblePosition - firstVisiblePosition) / 2

    val firstSmoothScrollPosition = (firstVisiblePosition - 2).coerceAtLeast(0)
    if (position < firstSmoothScrollPosition || position > lastVisiblePosition) {
        // invisible at all, fast scroll to position

        // recycler view doesn't scroll to exact position,
        // it scrolls to make target position visible
        // so add shift to target position and scroll direction is important here
        val targetPosition = if (position < firstSmoothScrollPosition) {
            // fast scroll backward
            (position - scrollWindowBottomPositionOffset - scrollWindowBottomOffset)
                .coerceAtLeast(0)
        } else {
            // fast scroll forward
            (position + scrollWindowBottomPositionOffset - scrollWindowBottomOffset)
                .coerceAtMost(adapter.itemCount - 1)
        }
        layoutManager.scrollToPosition(targetPosition)
        return
    }

    val scrollWindowTopPosition = firstVisiblePosition + scrollWindowTopOffset
    val scrollWindowBottomPosition = firstVisiblePosition + scrollWindowBottomPositionOffset
    val targetPosition = if (position < scrollWindowTopPosition) {
        // slightly atop if scroll window case
        // smooth scroll to top window border
        (position - scrollWindowTopOffset).coerceAtLeast(0)
    } else if (position > scrollWindowBottomPosition + scrollWindowBottomOffset) {
        // visible, smooth scroll to bottom window border
        position - scrollWindowBottomPositionOffset - scrollWindowBottomOffset
    } else {
        // in scroll window borders, do nothing
        return
    }


    val scroller = object: LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun calculateTimeForScrolling(dx: Int): Int {
            return 170
        }
    }
    scroller.targetPosition = targetPosition
    layoutManager.startSmoothScroll(scroller)
}