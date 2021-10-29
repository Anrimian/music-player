package com.github.anrimian.musicplayer.ui.settings.player.impls.view

import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemMediaPlayerBinding
import com.github.anrimian.musicplayer.ui.common.format.getMediaPlayerDescription
import com.github.anrimian.musicplayer.ui.common.format.getMediaPlayerName
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class MediaPlayerViewHolder(
    parent: ViewGroup
): BaseViewHolder(parent, R.layout.item_media_player) {

    private val viewBinding = ItemMediaPlayerBinding.bind(itemView)

    fun bind(id: Int) {
        viewBinding.tvMediaPlayerName.setText(getMediaPlayerName(id))
        viewBinding.tvMediaPlayerDescription.setText(getMediaPlayerDescription(id))
    }

}