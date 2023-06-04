package com.github.anrimian.musicplayer.ui.library.albums.list.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemAlbumBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

class AlbumViewHolder(
    parent: ViewGroup,
    itemClickListener: (Int, Album) -> Unit,
    itemLongClickListener: (Int, Album) -> Unit,
    onItemMenuClickListener: (View, Album) -> Unit
) : SelectableViewHolder(parent, R.layout.item_album), SwipeListener {

    private val viewBinding = ItemAlbumBinding.bind(itemView)

    private lateinit var album: Album

    private val backgroundDrawable = ItemDrawable()
    private val stateDrawable = ItemDrawable()
    private val rippleMaskDrawable = ItemDrawable()

    private var selected = false
    private var isSwiping = false

    init {
        viewBinding.clickableItem.setOnClickListener { itemClickListener(bindingAdapterPosition, album) }
        viewBinding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(v, album) }
        viewBinding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            itemLongClickListener(bindingAdapterPosition, album)
            true
        }

        backgroundDrawable.setColor(context.colorFromAttr(R.attr.listItemBackground))
        itemView.background = backgroundDrawable
        stateDrawable.setColor(Color.TRANSPARENT)
        viewBinding.clickableItem.background = stateDrawable
        viewBinding.clickableItem.foreground = RippleDrawable(
            ColorStateList.valueOf(context.colorFromAttr(android.R.attr.colorControlHighlight)),
            null,
            rippleMaskDrawable
        )
    }

    override fun release() {
        Components.getAppComponent().imageLoader().clearImage(viewBinding.ivMusicIcon)
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
            showStateColor(endColor, true)
        }
    }

    override fun onSwipeStateChanged(swipeOffset: Float) {
        val swiping = swipeOffset > 0.0f
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

    private fun selectImmediate() {
        showStateColor(selectionColor, false)
        selected = true
    }

    private fun showStateColor(@ColorInt color: Int, animate: Boolean) {
        if (animate) {
            stateDrawable.setColor(color)
        } else {
            ViewUtils.animateItemDrawableColor(stateDrawable, color)
        }
    }

    private fun showAlbumName() {
        val name = album.name
        viewBinding.tvAlbumName.text = name
        viewBinding.clickableItem.contentDescription = name
    }

    private fun showAdditionalInfo() {
        viewBinding.tvCompositionsCount.text = FormatUtils.formatAlbumAdditionalInfo(context, album)
    }

    private fun showCover() {
        Components.getAppComponent().imageLoader().displayImage(
            viewBinding.ivMusicIcon,
            album,
            R.drawable.ic_album_placeholder
        )
    }
}