package com.github.anrimian.musicplayer.ui.settings.player.impls.view

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemMediaPlayerBinding
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers
import com.github.anrimian.musicplayer.ui.common.format.getMediaPlayerName
import com.github.anrimian.musicplayer.ui.utils.onMotionDown
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class MediaPlayerViewHolder(
    parent: ViewGroup,
    private val onPlayerEnabled: (Int, Boolean) -> Unit,
    private val onDragButtonClick: (RecyclerView.ViewHolder) -> Unit
): BaseViewHolder(parent, R.layout.item_media_player) {

    private val binding = ItemMediaPlayerBinding.bind(itemView)

    private var item: Int = 0

    fun bind(id: Int) {
        this.item = id
        binding.tvMediaPlayerName.setText(getMediaPlayerName(id))
        binding.swMediaPlayer.setOnCheckedChangeListener { _, isChecked -> onPlayerEnabled(id, isChecked) }
        binding.ivDrag.onMotionDown { onDragButtonClick(this) }

        if (id == MediaPlayers.ANDROID_MEDIA_PLAYER) {
            binding.tvMediaPlayerSubtitle.visibility = View.VISIBLE
            binding.tvMediaPlayerSubtitle.setText(R.string.android_media_player_skip_silence_note)
        } else {
            binding.tvMediaPlayerSubtitle.visibility = View.GONE
        }
    }

    fun setEnabled(enabledItems: Set<Int>) {
        val isEnabled = enabledItems.contains(item)
        binding.swMediaPlayer.isChecked = isEnabled
    }

    fun setDisableAllowed(allowed: Boolean) {
        binding.ivDrag.isEnabled = allowed

        val enabled = !binding.swMediaPlayer.isChecked || allowed
        binding.swMediaPlayer.isEnabled = enabled
    }

}