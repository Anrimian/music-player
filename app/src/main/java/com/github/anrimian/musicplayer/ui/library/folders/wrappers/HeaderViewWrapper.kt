package com.github.anrimian.musicplayer.ui.library.folders.wrappers

import android.view.View
import com.github.anrimian.musicplayer.databinding.PartialStorageHeaderBinding
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource

/**
 * Created on 01.11.2017.
 */
class HeaderViewWrapper(private val viewBinding: PartialStorageHeaderBinding) {

    fun bind(folder: FolderFileSource) {
        viewBinding.tvParentPath.text = folder.name
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        viewBinding.headerClickableItem.setOnClickListener(listener)
    }

    fun setVisible(visible: Boolean) {
        viewBinding.root.visibility = if (visible) View.VISIBLE else View.GONE
    }

}