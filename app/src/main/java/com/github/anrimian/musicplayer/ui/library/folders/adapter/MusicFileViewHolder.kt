package com.github.anrimian.musicplayer.ui.library.folders.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemStorageMusicBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

/**
 * Created on 31.10.2017.
 */
class MusicFileViewHolder(
    parent: ViewGroup,
    onCompositionClickListener: (Int, CompositionFileSource) -> Unit,
    onLongClickListener: (Int, FileSource) -> Unit,
    iconClickListener: (Composition) -> Unit,
    menuClickListener: (View, CompositionFileSource) -> Unit
) : FileViewHolder(parent, R.layout.item_storage_music), SwipeListener {

    private val compositionItemWrapper: CompositionItemWrapper

    private lateinit var fileSource: CompositionFileSource

    private var selected = false
    private var selectedToMove = false
    private var isCurrent = false

    init {
        val binding = ItemStorageMusicBinding.bind(itemView)
        compositionItemWrapper = CompositionItemWrapper(itemView, iconClickListener) {
            onCompositionClickListener(bindingAdapterPosition, fileSource)
        }
        binding.btnActionsMenu.setOnClickListener { v -> menuClickListener(v, fileSource) }
        binding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            onLongClickListener(bindingAdapterPosition, fileSource)
            true
        }
    }

    fun bind(fileSource: CompositionFileSource, isCoversEnabled: Boolean) {
        this.fileSource = fileSource
        compositionItemWrapper.bind(fileSource.composition, isCoversEnabled)
    }

    fun update(fileSource: CompositionFileSource, payloads: List<*>) {
        this.fileSource = fileSource
        compositionItemWrapper.update(fileSource.composition, payloads)
        for (payload in payloads) {
            if (payload === Payloads.ITEM_SELECTED) {
                setSelected(true)
                return
            }
            if (payload === Payloads.ITEM_UNSELECTED) {
                setSelected(false)
                return
            }
        }
    }

    override fun release() {
        compositionItemWrapper.release()
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

    override fun setSelectedToMove(selected: Boolean) {
        if (selectedToMove != selected) {
            selectedToMove = selected
            val unselectedColor = Color.TRANSPARENT
            val selectedColor = moveSelectionColor
            val endColor = if (selected) selectedColor else unselectedColor
            compositionItemWrapper.showStateColor(endColor, true)
        }
    }

    override fun getFileSource(): FileSource {
        return fileSource
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
            isCurrent = fileSource.composition == currentComposition.composition
            isPlaying = isCurrent && currentComposition.isPlaying
        }
        showAsCurrentComposition(isCurrent)
        compositionItemWrapper.showAsPlaying(isPlaying, animate)
    }

    fun runHighlightAnimation() {
        compositionItemWrapper.runHighlightAnimation()
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
    private fun getPlaySelectionColor(): Int {
        return ColorFormatUtils.getPlayingCompositionColor(context, 25)
    }
}