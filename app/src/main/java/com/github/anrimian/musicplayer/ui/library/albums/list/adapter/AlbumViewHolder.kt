package com.github.anrimian.musicplayer.ui.library.albums.list.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemAlbumBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.wrappers.ItemBackgroundWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

class AlbumViewHolder(
    parent: ViewGroup,
    itemClickListener: (Int, Album) -> Unit,
    itemLongClickListener: (Int, Album) -> Unit,
    onItemMenuClickListener: (View, Album) -> Unit
) : SelectableViewHolder(parent, R.layout.item_album), SwipeListener {

    private val binding = ItemAlbumBinding.bind(itemView)

    private lateinit var album: Album

    private val itemBackgroundWrapper = ItemBackgroundWrapper(itemView, binding.clickableItem)

    private var selected = false
    private var isSwiping = false

    init {
        binding.clickableItem.setOnClickListener { itemClickListener(bindingAdapterPosition, album) }
        binding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(v, album) }
        binding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            itemLongClickListener(bindingAdapterPosition, album)
            true
        }
    }

    override fun release() {
        Components.getAppComponent().imageLoader().clearImage(binding.ivMusicIcon)
    }

    fun bind(album: Album) {
        this.album = album
        showAlbumName()
        showAdditionalInfo()
        showCover()
    }

    fun update(album: Album, payloads: List<*>) {
        this.album = album
        for (payload in payloads) {
            if (payload is List<*>) {
                update(album, payload)
            }
            if (payload === Payloads.NAME) {
                showAlbumName()
                continue
            }
            if (payload === Payloads.ARTIST) {
                showAdditionalInfo()
                continue
            }
            if (payload === Payloads.COMPOSITIONS_COUNT) {
                showAdditionalInfo()
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

    private fun showAlbumName() {
        val name = album.name
        binding.tvAlbumName.text = name
        binding.clickableItem.contentDescription = name
    }

    private fun showAdditionalInfo() {
        binding.tvCompositionsCount.text = FormatUtils.formatAlbumAdditionalInfo(getContext(), album)
    }

    private fun showCover() {
        Components.getAppComponent().imageLoader().displayImage(
            binding.ivMusicIcon,
            album,
            R.drawable.ic_album_placeholder
        )
    }
}