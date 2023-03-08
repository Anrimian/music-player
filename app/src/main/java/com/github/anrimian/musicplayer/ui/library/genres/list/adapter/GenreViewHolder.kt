package com.github.anrimian.musicplayer.ui.library.genres.list.adapter

import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemGenreBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class GenreViewHolder(
    parent: ViewGroup,
    itemClickListener: (Genre) -> Unit,
    longClickListener: (Genre) -> Unit
) : BaseViewHolder(parent, R.layout.item_genre) {

    private val viewBinding = ItemGenreBinding.bind(itemView)

    private lateinit var genre: Genre

    init {
        viewBinding.clickableItem.setOnClickListener { itemClickListener(genre) }
        ViewUtils.onLongClick(viewBinding.clickableItem) { longClickListener(genre) }
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
            }
            if (payload === Payloads.DURATION) {
                showAdditionalInfo()
            }
        }
    }

    private fun showGenreName() {
        viewBinding.tvGenreName.text = genre.name
    }

    private fun showAdditionalInfo() {
        val sb = DescriptionSpannableStringBuilder(context)
        sb.append(FormatUtils.formatCompositionsCount(context, genre.compositionsCount))
        val totalDuration = genre.totalDuration
        if (totalDuration != 0L) {
            sb.append(FormatUtils.formatMilliseconds(totalDuration))
        }
        viewBinding.tvAdditionalInfo.text = sb
    }
}