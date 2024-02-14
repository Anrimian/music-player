package com.github.anrimian.musicplayer.ui.settings.folders.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

class ExcludedFolderAdapter(
    recyclerView: RecyclerView,
    private val removeClickListener: (IgnoredFolder) -> Unit
) : DiffListAdapter<IgnoredFolder, ExcludedFolderViewHolder>(recyclerView, SimpleDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExcludedFolderViewHolder {
        return ExcludedFolderViewHolder(parent, removeClickListener)
    }

    override fun onBindViewHolder(holder: ExcludedFolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}