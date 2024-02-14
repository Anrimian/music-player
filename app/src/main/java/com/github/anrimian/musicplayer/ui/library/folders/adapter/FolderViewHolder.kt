package com.github.anrimian.musicplayer.ui.library.folders.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemStorageFolderBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.wrappers.ItemBackgroundWrapper
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

/**
 * Created on 31.10.2017.
 */
internal class FolderViewHolder(
    parent: ViewGroup,
    onFolderClickListener: (Int, FolderFileSource) -> Unit,
    onMenuClickListener: (View, FolderFileSource) -> Unit,
    onLongClickListener: (Int, FileSource) -> Unit,
) : FileViewHolder(parent, R.layout.item_storage_folder), SwipeListener {

    private val binding = ItemStorageFolderBinding.bind(itemView)

    private lateinit var folder: FolderFileSource

    private val itemBackgroundWrapper = ItemBackgroundWrapper(itemView, binding.clickableItem)

    private var isSelected = false
    private var isSelectedForMove = false
    private var isSwiping = false

    init {
        binding.clickableItem.setOnClickListener {
            onFolderClickListener(bindingAdapterPosition, folder)
        }
        ViewUtils.onLongClick(binding.clickableItem) {
            if (isSelected) {
                return@onLongClick
            }
            selectImmediate()
            onLongClickListener(bindingAdapterPosition, folder)
        }
        binding.btnActionsMenu.setOnClickListener { v -> onMenuClickListener(v, folder) }
    }

    override fun setSelected(selected: Boolean) {
        if (isSelected != selected) {
            isSelected = selected
            updateSelectionState()
        }
    }

    override fun setSelectedToMove(selected: Boolean) {
        if (isSelectedForMove != selected) {
            isSelectedForMove = selected
            updateSelectionState()
        }
    }

    override fun getFileSource(): FileSource {
        return folder
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

    fun bind(folderFileSource: FolderFileSource) {
        folder = folderFileSource
        showFolderName()
        showFilesCount()
    }

    fun update(folderFileSource: FolderFileSource, payloads: List<*>) {
        folder = folderFileSource
        bind(folderFileSource)//is it necessary here? We already have payloads
        for (payload in payloads) {
            if (payload is List<*>) {
                update(folderFileSource, payload)
            }
            if (payload === Payloads.ITEM_SELECTED) {
                setSelected(true)
                return
            }
            if (payload === Payloads.ITEM_UNSELECTED) {
                setSelected(false)
                return
            }
            if (payload === Payloads.NAME) {
                showFolderName()
            }
            if (payload === Payloads.FILES_COUNT) {
                showFilesCount()
            }
        }
    }

    private fun showFilesCount() {
        val text = FormatUtils.formatCompositionsCount(getContext(), folder.filesCount)
        binding.tvCompositionsCount.text = text
    }

    private fun showFolderName() {
        binding.tvFolderName.text = folder.name
    }

    private fun updateSelectionState() {
        val stateColor = if (isSelected) {
            selectionColor
        } else if (isSelectedForMove) {
            moveSelectionColor
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