package com.github.anrimian.musicplayer.ui.library.artists.list.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemArtistBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.wrappers.ItemBackgroundWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

class ArtistViewHolder(
    parent: ViewGroup,
    itemClickListener: (Int, Artist) -> Unit,
    itemLongClickListener: (Int, Artist) -> Unit,
    onItemMenuClickListener: (View, Artist) -> Unit,
) : SelectableViewHolder(parent, R.layout.item_artist), SwipeListener {

    private val binding = ItemArtistBinding.bind(itemView)

    private lateinit var artist: Artist

    private val itemBackgroundWrapper = ItemBackgroundWrapper(itemView, binding.clickableItem)

    private var selected = false
    private var isSwiping = false

    init {
        binding.clickableItem.setOnClickListener { itemClickListener(bindingAdapterPosition, artist) }
        binding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(v, artist) }
        binding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            itemLongClickListener(bindingAdapterPosition, artist)
            true
        }
    }

    fun bind(artist: Artist) {
        this.artist = artist
        showAuthorName()
        showCompositionsCount()
    }

    fun update(artist: Artist, payloads: List<*>) {
        this.artist = artist
        for (payload in payloads) {
            if (payload is List<*>) {
                update(artist, payload)
            }
            if (payload === Payloads.NAME) {
                showAuthorName()
                continue
            }
            if (payload === Payloads.COMPOSITIONS_COUNT) {
                showCompositionsCount()
            }
        }
    }

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

    private fun showAuthorName() {
        val name = artist.name
        binding.tvArtistName.text = name
        binding.clickableItem.contentDescription = name
    }

    private fun showCompositionsCount() {
        binding.tvAdditionalInfo.text = FormatUtils.formatArtistAdditionalInfo(getContext(), artist)
    }
}