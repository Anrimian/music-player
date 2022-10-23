package com.github.anrimian.musicplayer.ui.library.compositions.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemStorageMusicBinding
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

/**
 * Created on 31.10.2017.
 */
class MusicViewHolder(
    parent: ViewGroup,
    onCompositionClickListener: (Int, Composition) -> Unit,
    onLongClickListener: (Int, Composition) -> Unit,
    iconClickListener: (Int, Composition) -> Unit,
    menuClickListener: (View, Int, Composition) -> Unit
) : SelectableViewHolder(parent, R.layout.item_storage_music), SwipeListener {

    private val compositionItemWrapper: CompositionItemWrapper

    private lateinit var composition: Composition

    private var selected = false
    private var isCurrent = false

    init {
        val binding = ItemStorageMusicBinding.bind(itemView)
        compositionItemWrapper = CompositionItemWrapper(
            itemView,
            { composition -> iconClickListener(bindingAdapterPosition, composition) }
        ) { composition -> onCompositionClickListener(bindingAdapterPosition, composition) }

        binding.btnActionsMenu.setOnClickListener { v ->
            menuClickListener(v, bindingAdapterPosition, composition)
        }

        binding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            onLongClickListener(bindingAdapterPosition, composition)
            true
        }
    }

    override fun release() {
        compositionItemWrapper.release()
    }

    fun bind(composition: Composition, isCoversEnabled: Boolean) {
        this.composition = composition
        compositionItemWrapper.bind(composition, isCoversEnabled)
    }

    fun update(composition: Composition, payloads: List<*>) {
        this.composition = composition
        compositionItemWrapper.update(composition, payloads)
    }

    fun setCoversVisible(isCoversEnabled: Boolean) {
        compositionItemWrapper.showCompositionImage(isCoversEnabled)
    }

    override fun setSelected(selected: Boolean) {
        if (this.selected != selected) {
            this.selected = selected
            val unselectedColor =
                if (!selected && isCurrent) getPlaySelectionColor() else Color.TRANSPARENT
            val selectedColor = selectionColor
            val endColor = if (selected) selectedColor else unselectedColor
            compositionItemWrapper.showStateColor(endColor, true)
        }
    }

    override fun onSwipeStateChanged(swipeOffset: Float) {
        compositionItemWrapper.showAsSwipingItem(swipeOffset)
    }

    fun showCurrentComposition(
        currentComposition: CurrentComposition?,
        animate: Boolean
    ) {
        var isCurrent = false
        var isPlaying = false
        if (currentComposition != null) {
            isCurrent = composition == currentComposition.composition
            isPlaying = isCurrent && currentComposition.isPlaying
        }
        showAsCurrentComposition(isCurrent)
        compositionItemWrapper.showAsPlaying(isPlaying, animate)
    }

    private fun showAsCurrentComposition(isCurrent: Boolean) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent
            if (!selected) {
                compositionItemWrapper.showAsCurrentComposition(isCurrent)
            }
        }
    }

    private fun selectImmediate() {
        compositionItemWrapper.showStateColor(selectionColor, false)
        selected = true
    }

    @ColorInt
    private fun getPlaySelectionColor() = ColorFormatUtils.getPlayingCompositionColor(context, 25)

}