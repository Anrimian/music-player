package com.github.anrimian.musicplayer.ui.library.artists.list.adapter

import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemArtistBinding
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class ArtistViewHolder(
    parent: ViewGroup,
    itemClickListener: (Artist) -> Unit,
    onItemMenuClickListener: (View, Artist) -> Unit
) : BaseViewHolder(parent, R.layout.item_artist) {

    private val viewBinding = ItemArtistBinding.bind(itemView)

    private lateinit var artist: Artist

    init {
        viewBinding.clickableItem.setOnClickListener { itemClickListener(artist) }
        viewBinding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(v, artist) }
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

    private fun showAuthorName() {
        val name = artist.name
        viewBinding.tvArtistName.text = name
        viewBinding.clickableItem.contentDescription = name
    }

    private fun showCompositionsCount() {
        viewBinding.tvAdditionalInfo.text = FormatUtils.formatArtistAdditionalInfo(context, artist)
    }
}