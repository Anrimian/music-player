package com.github.anrimian.musicplayer.ui.library.artists.list.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.utils.ArtistHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

class ArtistsAdapter(
    recyclerView: RecyclerView,
    private val itemClickListener: (Artist) -> Unit,
    private val onItemMenuClickListener: (View, Artist) -> Unit
) : DiffListAdapter<Artist, ArtistViewHolder>(
    recyclerView,
    SimpleDiffItemCallback(ArtistHelper::areSourcesTheSame, ArtistHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        return ArtistViewHolder(parent, itemClickListener, onItemMenuClickListener)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: ArtistViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        holder.update(getItem(position)!!, payloads)
    }
}