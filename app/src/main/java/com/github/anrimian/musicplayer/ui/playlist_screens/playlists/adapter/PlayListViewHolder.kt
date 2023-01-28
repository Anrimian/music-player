package com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemPlayListBinding
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatPlaylistAdditionalInfo

class PlayListViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    onItemClickListener: (PlayList) -> Unit,
    onItemMenuClickListener: (PlayList, View) -> Unit,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_play_list, parent, false)) {

    private val viewBinding = ItemPlayListBinding.bind(itemView)

    private lateinit var playList: PlayList

    init {
        itemView.setOnClickListener { onItemClickListener(playList) }
        viewBinding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(playList, v) }
    }

    fun bind(playList: PlayList) {
        this.playList = playList
        viewBinding.tvPlayListName.text = playList.name
        showAdditionalInfo()
    }

    private fun showAdditionalInfo() {
        viewBinding.tvAdditionalInfo.text = formatPlaylistAdditionalInfo(getContext(), playList)
    }

    private fun getContext() = itemView.context

}