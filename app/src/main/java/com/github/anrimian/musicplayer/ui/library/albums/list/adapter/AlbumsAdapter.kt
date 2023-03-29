package com.github.anrimian.musicplayer.ui.library.albums.list.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.utils.AlbumHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter
import java.util.Collections

class AlbumsAdapter(
    recyclerView: RecyclerView,
    private val itemClickListener: (Album) -> Unit,
    private val onItemMenuClickListener: (View, Album) -> Unit
) : DiffListAdapter<Album, AlbumViewHolder>(
    recyclerView,
    SimpleDiffItemCallback(AlbumHelper::areSourcesTheSame, AlbumHelper::getChangePayload)
) ,RecyclerRowMoveCallback.RecyclerViewRowTouchHelperContract{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(parent, itemClickListener, onItemMenuClickListener)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MyViewModel(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

    override fun onRowMoved(from: Int, to: Int) {
        var list= currentList;
        if (from < to) {
            for (i in from until to) {

                submitList(list);
            }
        } else {
            for (i in from downTo to + 1) {

                submitList(list)
            }
        }
        notifyItemMoved(from, to)
    }

    override fun onRowSelected(myViewHolder: MyViewModel?) {
        TODO("Not yet implemented")
    }

    override fun onRowClear(myViewHolder: MyViewModel?) {
        TODO("Not yet implemented")
    }
}