package com.github.anrimian.musicplayer.ui.settings.folders.view

import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemExcludedFolderBinding
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class ExcludedFolderViewHolder(
    parent: ViewGroup,
    removeClickListener: (IgnoredFolder) -> Unit
) : BaseViewHolder(parent, R.layout.item_excluded_folder) {

    private val binding = ItemExcludedFolderBinding.bind(itemView)

    private lateinit var folder: IgnoredFolder

    init {
        binding.btnRemove.setOnClickListener { removeClickListener(folder) }
    }

    fun bind(folder: IgnoredFolder) {
        this.folder = folder
        binding.tvFolderName.text = folder.relativePath
    }

}