package com.github.anrimian.musicplayer.ui.playlists.choose.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.utils.PlayListHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

class PlayListsAdapter(
    recyclerView: RecyclerView,
    private val onItemClickListener: (Playlist) -> Unit,
    private val onItemMenuClickListener: (Playlist, View) -> Unit
) : DiffListAdapter<Playlist, PlayListViewHolder>(
    recyclerView, SimpleDiffItemCallback(PlayListHelper::areItemsTheSame, PlayListHelper::areSourcesTheSame)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        return PlayListViewHolder(parent, onItemClickListener, onItemMenuClickListener)
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}