package com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemPlayListBinding
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatPlaylistAdditionalInfo
import com.github.anrimian.musicplayer.ui.common.format.wrappers.ItemBackgroundWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

class PlayListViewHolder(
    parent: ViewGroup,
    onItemClickListener: (Int, PlayList) -> Unit,
    itemLongClickListener: (Int, PlayList) -> Unit,
    onItemMenuClickListener: (PlayList, View) -> Unit,
) : SelectableViewHolder(parent, R.layout.item_play_list), SwipeListener {

    private val binding = ItemPlayListBinding.bind(itemView)

    private lateinit var playList: PlayList

    private val itemBackgroundWrapper = ItemBackgroundWrapper(itemView, binding.clickableItem)

    private var selected = false
    private var isSwiping = false

    init {
        binding.clickableItem.setOnClickListener { onItemClickListener(bindingAdapterPosition, playList) }
        binding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            itemLongClickListener(bindingAdapterPosition, playList)
            true
        }
        binding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(playList, v) }
    }

    fun bind(playList: PlayList) {
        this.playList = playList
        binding.tvPlayListName.text = playList.name
        showAdditionalInfo()
    }

    fun getPlaylist() = playList

    override fun setSelected(selected: Boolean) {
        if (this.selected != selected) {
            this.selected = selected
            val unselectedColor = Color.TRANSPARENT
            val selectedColor = selectionColor
            val endColor = if (selected) selectedColor else unselectedColor
            itemBackgroundWrapper.showStateColor(endColor, true)
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

    private fun selectImmediate() {
        itemBackgroundWrapper.showStateColor(selectionColor, false)
        selected = true
    }

    private fun showAdditionalInfo() {
        binding.tvAdditionalInfo.text = formatPlaylistAdditionalInfo(getContext(), playList)
    }

}