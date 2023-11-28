package com.github.anrimian.musicplayer.ui.library.albums.items.adaptar

import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionViewHolder

class AlbumCompositionViewHolder(
    parent: ViewGroup,
    onCompositionClickListener: (Int, AlbumComposition) -> Unit,
    onLongClickListener: (Int, AlbumComposition) -> Unit,
    iconClickListener: (Int, AlbumComposition) -> Unit,
    menuClickListener: (View, Int, AlbumComposition) -> Unit
): CompositionViewHolder<AlbumComposition>(
    parent,
    onCompositionClickListener,
    onLongClickListener,
    iconClickListener,
    menuClickListener
) {

    override fun createCompositionItemWrapper(
        onCompositionClickListener: (Int, AlbumComposition) -> Unit,
        iconClickListener: (Int, AlbumComposition) -> Unit,
    ): CompositionItemWrapper<AlbumComposition> {
        return AlbumCompositionItemWrapper(
            itemView,
            { composition -> iconClickListener(bindingAdapterPosition, composition) },
            { composition -> onCompositionClickListener(bindingAdapterPosition, composition) }
        )
    }

}