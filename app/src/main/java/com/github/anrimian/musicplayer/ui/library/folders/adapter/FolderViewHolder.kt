package com.github.anrimian.musicplayer.ui.library.folders.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemStorageFolderBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable
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

    private val viewBinding = ItemStorageFolderBinding.bind(itemView)

    private lateinit var folder: FolderFileSource

    private val backgroundDrawable = ItemDrawable()
    private val stateDrawable = ItemDrawable()
    private val rippleMaskDrawable = ItemDrawable()

    private var isSelected = false
    private var isSelectedForMove = false
    private var isSwiping = false

    init {
        viewBinding.clickableItem.setOnClickListener {
            onFolderClickListener(bindingAdapterPosition, folder)
        }
        ViewUtils.onLongClick(viewBinding.clickableItem) {
            if (isSelected) {
                return@onLongClick
            }
            selectImmediate()
            onLongClickListener(bindingAdapterPosition, folder)
        }
        viewBinding.btnActionsMenu.setOnClickListener { v -> onMenuClickListener(v, folder) }

        backgroundDrawable.setColor(
            AndroidUtils.getColorFromAttr(context, R.attr.listItemBackground)
        )
        itemView.background = backgroundDrawable
        stateDrawable.setColor(Color.TRANSPARENT)
        viewBinding.clickableItem.background = stateDrawable
        viewBinding.clickableItem.foreground = RippleDrawable(
            ColorStateList.valueOf(
                AndroidUtils.getColorFromAttr(context, android.R.attr.colorControlHighlight)
            ),
            null,
            rippleMaskDrawable
        )
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

        viewBinding.divider.translationX = swipeOffset

        if (isSwiping != swiping) {
            isSwiping = swiping
            val swipedCorners = context.resources.getDimension(R.dimen.swiped_item_corners)
            val from: Float = if (swiping) 0f else swipedCorners
            val to: Float = if (swiping) swipedCorners else 0f
            val duration = context.resources.getInteger(R.integer.swiped_item_animation_time)
            AndroidUtils.animateItemDrawableCorners(
                from,
                to,
                duration,
                backgroundDrawable,
                stateDrawable,
                rippleMaskDrawable
            )
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
        val text = FormatUtils.formatCompositionsCount(context, folder.filesCount)
        viewBinding.tvCompositionsCount.text = text
    }

    private fun showFolderName() {
        viewBinding.tvFolderName.text = folder.name
    }

    private fun updateSelectionState() {
        val stateColor = if (isSelected) {
            selectionColor
        } else if (isSelectedForMove) {
            moveSelectionColor
        } else {
            Color.TRANSPARENT
        }
        ViewUtils.animateItemDrawableColor(stateDrawable, stateColor)
    }

    private fun selectImmediate() {
        stateDrawable.setColor(selectionColor)
        isSelected = true
    }
}