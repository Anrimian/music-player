package com.github.anrimian.musicplayer.ui.library.albums.list.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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


    @SuppressLint("NotifyDataSetChanged")
    override fun onRowMoved(from: Int, to: Int) {
//        val list= ArrayList<Album>();
//        currentList.forEach {
//            list.add(it);
//        }

        if (from < to) {
            for (i in from until to) {
//                Log.i("AlbumsAdapter swap", "from: "  + list[from] + "to: " + list[to])
//
//                Collections.swap(list , from , to)
                swap(from  , to)
                notifyDataSetChanged()
            }
        } else {
            for (i in from downTo to + 1) {
//                Log.i("AlbumsAdapter swap", "from: "  + list[from] + "to: " + list[to])
//
//                Collections.swap(list , from , to)
                swap(from  , to)
                notifyDataSetChanged()
            }
        }
        notifyItemMoved(from, to)
    }

    override fun onRowSelected(myViewHolder: AlbumViewHolder?) {
        return
    }

    override fun onRowClear(myViewHolder: AlbumViewHolder?) {

        return
    }


}