package com.github.anrimian.musicplayer.ui.playlist_screens.choose.adapter;

import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemPlayListBinding
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatPlaylistAdditionalInfo
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class PlayListViewHolder(
    parent: ViewGroup,
    onItemClickListener: (PlayList) -> Unit,
    onItemMenuClickListener: (PlayList, View) -> Unit,
) : BaseViewHolder(parent, R.layout.item_play_list) {

    private val binding = ItemPlayListBinding.bind(itemView)

    private lateinit var playList: PlayList

    init {
        itemView.setOnClickListener { onItemClickListener(playList) }
        binding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(playList, v) }
    }

    fun bind(playList: PlayList) {
        this.playList = playList
        binding.tvPlayListName.text = playList.name
        showAdditionalInfo()
    }

    private fun showAdditionalInfo() {
        binding.tvAdditionalInfo.text = formatPlaylistAdditionalInfo(getContext(), playList)
    }
}