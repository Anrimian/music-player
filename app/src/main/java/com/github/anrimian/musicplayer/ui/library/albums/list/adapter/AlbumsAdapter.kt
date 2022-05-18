package com.github.anrimian.musicplayer.ui.library.albums.list.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.utils.AlbumHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

class AlbumsAdapter(
    recyclerView: RecyclerView,
    private val itemClickListener: (Album) -> Unit,
    private val onItemMenuClickListener: (View, Album) -> Unit
) : DiffListAdapter<Album, AlbumViewHolder>(
    recyclerView,
    SimpleDiffItemCallback(AlbumHelper::areSourcesTheSame, AlbumHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(parent, itemClickListener, onItemMenuClickListener)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: AlbumViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        holder.update(getItem(position), payloads)
    }

    override fun onViewRecycled(holder: AlbumViewHolder) {
        super.onViewRecycled(holder)
        holder.release()
    }
}