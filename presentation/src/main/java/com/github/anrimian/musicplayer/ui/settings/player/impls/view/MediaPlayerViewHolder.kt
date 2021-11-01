package com.github.anrimian.musicplayer.ui.settings.player.impls.view

import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemMediaPlayerBinding
import com.github.anrimian.musicplayer.ui.common.format.getMediaPlayerDescription
import com.github.anrimian.musicplayer.ui.common.format.getMediaPlayerName
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class MediaPlayerViewHolder(
    parent: ViewGroup,
    private val onPlayerEnabled: (Int, Boolean) -> Unit
): BaseViewHolder(parent, R.layout.item_media_player) {

    private val viewBinding = ItemMediaPlayerBinding.bind(itemView)

    private var item: Int = 0

    fun bind(id: Int) {
        this.item = id
        viewBinding.tvMediaPlayerName.setText(getMediaPlayerName(id))
        viewBinding.tvMediaPlayerDescription.setText(getMediaPlayerDescription(id))
        viewBinding.swMediaPlayer.setOnCheckedChangeListener { _, isChecked -> onPlayerEnabled(id, isChecked) }
        viewBinding.ivDrag.setOnClickListener {  }
    }

    fun setEnabled(enabledItems: Set<Int>) {
        viewBinding.swMediaPlayer.isChecked = enabledItems.contains(item)
    }

    fun setDisableAllowed(allowed: Boolean) {
        val enabled = !viewBinding.swMediaPlayer.isChecked || allowed
        viewBinding.swMediaPlayer.isEnabled = enabled
        viewBinding.ivDrag.isEnabled = enabled
    }

}