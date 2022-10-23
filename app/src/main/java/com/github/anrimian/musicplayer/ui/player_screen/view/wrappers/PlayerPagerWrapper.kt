package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers

import androidx.viewpager2.widget.ViewPager2
import com.github.anrimian.musicplayer.databinding.PartialQueueToolbarBinding
import com.github.anrimian.musicplayer.ui.utils.views.delegate.*

fun attachPlayerPagerWrapper(
    viewPager: ViewPager2,
    toolbarPlayQueueBinding: PartialQueueToolbarBinding,
    onPageChanged: (Int) -> Unit
) {
    val pagerSlideDelegate = DelegateManager()
    pagerSlideDelegate.addDelegate(MotionLayoutDelegate(toolbarPlayQueueBinding.root))
    pagerSlideDelegate.addDelegate(BoundValuesDelegate(
        0.4f,
        0.9f,
        VisibilityDelegate(toolbarPlayQueueBinding.tvQueueTitle))
    )
    pagerSlideDelegate.addDelegate(TransitionVisibilityDelegate(
        0.02f,
        0.04f,
        0.96f,
        0.98f,
        toolbarPlayQueueBinding.transitionShadow,
    ))
    pagerSlideDelegate.addDelegate(BoundValuesDelegate(
        0.85f,
        0.97f,
        VisibilityDelegate(toolbarPlayQueueBinding.tvQueueSubtitle))
    )
    pagerSlideDelegate.addDelegate(ReverseDelegate(BoundValuesDelegate(
        0.3f,
        0.9f,
        VisibilityDelegate(toolbarPlayQueueBinding.tvLyricsTitle))
    ))
    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onPageChanged(position)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
            val alpha = if (position == 1) 1 - positionOffset else positionOffset
            pagerSlideDelegate.onSlide(alpha)
        }
    })
}