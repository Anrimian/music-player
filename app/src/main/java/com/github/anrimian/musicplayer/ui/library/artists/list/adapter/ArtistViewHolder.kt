package com.github.anrimian.musicplayer.ui.library.artists.list.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemArtistBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

class ArtistViewHolder(
    parent: ViewGroup,
    itemClickListener: (Int, Artist) -> Unit,
    itemLongClickListener: (Int, Artist) -> Unit,
    onItemMenuClickListener: (View, Artist) -> Unit,
) : SelectableViewHolder(parent, R.layout.item_artist), SwipeListener {

    private val viewBinding = ItemArtistBinding.bind(itemView)

    private lateinit var artist: Artist

    private val backgroundDrawable = ItemDrawable()
    private val stateDrawable = ItemDrawable()
    private val rippleMaskDrawable = ItemDrawable()

    private var selected = false
    private var isSwiping = false

    init {
        viewBinding.clickableItem.setOnClickListener { itemClickListener(bindingAdapterPosition, artist) }
        viewBinding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(v, artist) }
        viewBinding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            itemLongClickListener(bindingAdapterPosition, artist)
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

    private fun showAuthorName() {
        val name = artist.name
        viewBinding.tvArtistName.text = name
        viewBinding.clickableItem.contentDescription = name
    }

    private fun showCompositionsCount() {
        viewBinding.tvAdditionalInfo.text = FormatUtils.formatArtistAdditionalInfo(context, artist)
    }
}