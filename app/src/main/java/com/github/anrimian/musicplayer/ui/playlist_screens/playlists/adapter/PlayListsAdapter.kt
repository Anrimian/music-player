package com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.PlayListHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

class PlayListsAdapter(
    recyclerView: RecyclerView,
    private val onItemClickListener: (PlayList) -> Unit,
    private val onItemLongClickListener: (PlayList, View) -> Unit
) : DiffListAdapter<PlayList, PlayListViewHolder>(
    recyclerView, SimpleDiffItemCallback(PlayListHelper::areSourcesTheSame)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        return PlayListViewHolder(
            LayoutInflater.from(parent.context),
            parent,
            onItemClickListener,
            onItemLongClickListener
        )
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        val playList = getItem(position)
        holder.bind(playList)
    }

}