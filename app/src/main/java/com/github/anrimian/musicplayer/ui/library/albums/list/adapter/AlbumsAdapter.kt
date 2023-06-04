package com.github.anrimian.musicplayer.ui.library.albums.list.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.utils.AlbumHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

class AlbumsAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedAlbums: Set<Album>,
    private val itemClickListener: (Int, Album) -> Unit,
    private val itemLongClickListener: (Int, Album) -> Unit,
    private val onItemMenuClickListener: (View, Album) -> Unit
) : MvpDiffAdapter<Album, AlbumViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(AlbumHelper::areSourcesTheSame, AlbumHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(
            parent,
            itemClickListener,
            itemLongClickListener,
            onItemMenuClickListener
        )
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val album = getItem(position)
        holder.bind(album)

        val selected = selectedAlbums.contains(album)
        holder.setSelected(selected)
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