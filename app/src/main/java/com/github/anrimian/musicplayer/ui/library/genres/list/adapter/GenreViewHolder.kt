package com.github.anrimian.musicplayer.ui.library.genres.list.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemGenreBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder
import com.github.anrimian.musicplayer.ui.common.format.wrappers.ItemBackgroundWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener

class GenreViewHolder(
    parent: ViewGroup,
    itemClickListener: (Int, Genre) -> Unit,
    itemLongClickListener: (Int, Genre) -> Unit,
    onItemMenuClickListener: (View, Genre) -> Unit
) : SelectableViewHolder(parent, R.layout.item_genre), SwipeListener {

    private val binding = ItemGenreBinding.bind(itemView)

    private lateinit var genre: Genre

    private val itemBackgroundWrapper = ItemBackgroundWrapper(itemView, binding.clickableItem)

    private var selected = false
    private var isSwiping = false

    init {
        binding.clickableItem.setOnClickListener { itemClickListener(bindingAdapterPosition, genre) }
        binding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(v, genre) }
        binding.clickableItem.setOnLongClickListener {
            if (selected) {
                return@setOnLongClickListener false
            }
            selectImmediate()
            itemLongClickListener(bindingAdapterPosition, genre)
            true
        }
    }

    fun bind(genre: Genre) {
        this.genre = genre
        showGenreName()
        showAdditionalInfo()
    }

    fun update(genre: Genre, payloads: List<*>) {
        this.genre = genre
        for (payload in payloads) {
            if (payload is List<*>) {
                update(genre, payload)
            }
            if (payload === Payloads.NAME) {
                showGenreName()
                continue
            }
            if (payload === Payloads.COMPOSITIONS_COUNT) {
                showAdditionalInfo()
                continue
            }
            if (payload === Payloads.DURATION) {
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

    private fun showGenreName() {
        binding.tvGenreName.text = genre.name
    }

    private fun showAdditionalInfo() {
        val sb = DescriptionSpannableStringBuilder(getContext())
        sb.append(FormatUtils.formatCompositionsCount(getContext(), genre.compositionsCount))
        val totalDuration = genre.totalDuration
        if (totalDuration != 0L) {
            sb.append(FormatUtils.formatMilliseconds(totalDuration))
        }
        binding.tvAdditionalInfo.text = sb
    }
}