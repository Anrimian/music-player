package com.github.anrimian.musicplayer.ui.library.artists.list.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.utils.ArtistHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

class ArtistsAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedArtists: Set<Artist>,
    private val itemClickListener: (Int, Artist) -> Unit,
    private val itemLongClickListener: (Int, Artist) -> Unit,
    private val onItemMenuClickListener: (View, Artist) -> Unit
) : MvpDiffAdapter<Artist, ArtistViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(ArtistHelper::areSourcesTheSame, ArtistHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        return ArtistViewHolder(
            parent,
            itemClickListener,
            itemLongClickListener,
            onItemMenuClickListener
        )
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val artist = getItem(position)
        holder.bind(artist)

        val selected = selectedArtists.contains(artist)
        holder.setSelected(selected)
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
        for (payload in payloads) {
            if (payload === Payloads.ITEM_SELECTED) {
                holder.setSelected(true)
                return
            }
            if (payload === Payloads.ITEM_UNSELECTED) {
                holder.setSelected(false)
                return
            }
        }
        holder.update(getItem(position), payloads)
    }

    fun setItemSelected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_SELECTED)
    }

    fun setItemUnselected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_UNSELECTED)
    }

    fun setItemsSelected(selected: Boolean) {
        forEachHolder { holder -> holder.setSelected(selected) }
    }
}