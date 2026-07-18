package com.github.anrimian.musicplayer.wear.ui.queue

import android.view.ViewGroup
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder
import com.github.anrimian.musicplayer.wear.R
import com.github.anrimian.musicplayer.wear.databinding.ItemQueueBinding
import com.github.anrimian.musicplayer.wear.domain.models.PlayQueueItem

class PlayQueueViewHolder(
    parent: ViewGroup,
): BaseViewHolder(parent, R.layout.item_queue) {

    private val binding = ItemQueueBinding.bind(itemView)

    fun bind(item: PlayQueueItem) {
        binding.tvTitle.text = item.title
        binding.tvArtist.text = item.artist + " - " + item.duration
    }

}