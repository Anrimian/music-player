package com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.PlayListHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

class PlayListsAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedPlaylists: Set<PlayList>,
    private val onItemClickListener: (Int, PlayList) -> Unit,
    private val itemLongClickListener: (Int, PlayList) -> Unit,
    private val onItemLongClickListener: (PlayList, View) -> Unit
) : MvpDiffAdapter<PlayList, PlayListViewHolder>(
    lifecycleOwner, recyclerView, SimpleDiffItemCallback(PlayListHelper::areSourcesTheSame)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        return PlayListViewHolder(
            parent,
            onItemClickListener,
            itemLongClickListener,
            onItemLongClickListener
        )
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val playList = getItem(position)
        holder.bind(playList)

        val selected = selectedPlaylists.contains(playList)
        holder.setSelected(selected)
    }

    override fun onBindViewHolder(
        holder: PlayListViewHolder,
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