package com.github.anrimian.musicplayer.ui.editor.composition.list

import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemGenreChipBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class GenreChipViewHolder(
    parent: ViewGroup,
    onClickListener: (String) -> Unit,
    onRemoveClickListener: (String) -> Unit,
) : BaseViewHolder(parent, R.layout.item_genre_chip) {

    private val binding = ItemGenreChipBinding.bind(itemView)

    private lateinit var genre: String

    init {
        binding.root.setOnClickListener { onClickListener(genre) }
        binding.ivRemove.setOnClickListener { onRemoveClickListener(genre) }
    }

    fun bind(genre: String) {
        this.genre = genre
        showName()
    }

    fun update(genre: String, payloads: List<*>) {
        this.genre = genre
        for (payload in payloads) {
            if (payload is List<*>) {
                update(genre, payload)
            }
            if (payload == Payloads.NAME) {
                showName()
            }
        }
    }

    private fun showName() {
        binding.tvGenre.text = genre
    }
}