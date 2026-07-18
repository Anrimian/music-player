package com.github.anrimian.musicplayer.ui.library.folders.volumes.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.storage.providers.MediaStoreUtils
import com.github.anrimian.musicplayer.databinding.ItemLibraryVolumeBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.ui.common.format.formatVolumeAdditionalInfo
import com.github.anrimian.musicplayer.ui.common.format.wrappers.ItemBackgroundWrapper
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

class VolumeViewHolder(
    parent: ViewGroup,
    onItemClickListener: (Int, Volume) -> Unit,
    onMenuClickListener: (View, Volume) -> Unit,
    onLongClickListener: (Int, Volume) -> Unit,
) : SelectableViewHolder(parent, R.layout.item_library_volume), SwipeListener {

    private val binding = ItemLibraryVolumeBinding.bind(itemView)

    private lateinit var volume: Volume

    private val itemBackgroundWrapper = ItemBackgroundWrapper(itemView, binding.clickableItem)

    private var isSelected = false
    private var isSwiping = false

    init {
        binding.clickableItem.setOnClickListener {
            onItemClickListener(bindingAdapterPosition, volume)
        }
        ViewUtils.onLongClick(binding.clickableItem) {
            if (isSelected) {
                return@onLongClick
            }
            selectImmediate()
            onLongClickListener(bindingAdapterPosition, volume)
        }
        binding.btnActionsMenu.setOnClickListener { v -> onMenuClickListener(v, volume) }
    }

    override fun setSelected(selected: Boolean) {
        if (isSelected != selected) {
            isSelected = selected
            updateSelectionState()
        }
    }

    override fun onSwipeStateChanged(swipeOffset: Float) {
        val swiping = swipeOffset > 0.0f
        if (isSwiping != swiping) {
            isSwiping = swiping
            val swipedCorners = getResources().getDimension(R.dimen.swiped_item_corners)
            val from: Float = if (swiping) 0f else swipedCorners
            val to: Float = if (swiping) swipedCorners else 0f
            val duration = getResources().getInteger(R.integer.swiped_item_animation_time)
            itemBackgroundWrapper.animateItemDrawableCorners(from, to, duration)
        }
    }

    fun bind(volume: Volume) {
        this.volume = volume
        binding.tvName.text = MediaStoreUtils.getVolumeDisplayName(getContext(), volume.storageKey)
        binding.tvPath.text = formatVolumeAdditionalInfo(getContext(), volume)
    }

    fun update(volume: Volume, payloads: List<*>) {
        this.volume = volume
        bind(volume)
        for (payload in payloads) {
            if (payload == Payloads.ITEM_SELECTED) {
                setSelected(true)
                return
            }
            if (payload == Payloads.ITEM_UNSELECTED) {
                setSelected(false)
                return
            }
        }
    }

    private fun updateSelectionState() {
        val stateColor = if (isSelected) {
            selectionColor
        } else {
            Color.TRANSPARENT
        }
        itemBackgroundWrapper.showStateColor(stateColor, true)
    }

    private fun selectImmediate() {
        itemBackgroundWrapper.showStateColor(selectionColor, false)
        isSelected = true
    }

}