package com.github.anrimian.musicplayer.ui.settings.player.impls.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemMediaPlayerBinding
import com.github.anrimian.musicplayer.ui.common.format.getMediaPlayerName
import com.github.anrimian.musicplayer.ui.utils.onMotionDown
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class MediaPlayerViewHolder(
    parent: ViewGroup,
    private val onPlayerEnabled: (Int, Boolean) -> Unit,
    private val onDragButtonClick: (RecyclerView.ViewHolder) -> Unit
): BaseViewHolder(parent, R.layout.item_media_player) {

    private val viewBinding = ItemMediaPlayerBinding.bind(itemView)

    private var item: Int = 0

    fun bind(id: Int) {
        this.item = id
        viewBinding.tvMediaPlayerName.setText(getMediaPlayerName(id))
        viewBinding.swMediaPlayer.setOnCheckedChangeListener { _, isChecked -> onPlayerEnabled(id, isChecked) }
        viewBinding.ivDrag.onMotionDown { onDragButtonClick(this) }
    }

    fun setEnabled(enabledItems: Set<Int>) {
        val isEnabled = enabledItems.contains(item)
        viewBinding.swMediaPlayer.isChecked = isEnabled
    }

    fun setDisableAllowed(allowed: Boolean) {
        viewBinding.ivDrag.isEnabled = allowed

        val enabled = !viewBinding.swMediaPlayer.isChecked || allowed
        viewBinding.swMediaPlayer.isEnabled = enabled
    }

}