package com.github.anrimian.musicplayer.ui.library.albums.items.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionViewHolder
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback

class AlbumCompositionsAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    selectedCompositions: Set<AlbumComposition>,
    private val onCompositionClickListener: (Int, AlbumComposition) -> Unit,
    private val onLongClickListener: (Int, AlbumComposition) -> Unit,
    private val iconClickListener: (Int, AlbumComposition) -> Unit,
    private val menuClickListener: (View, Int, AlbumComposition) -> Unit,
): CompositionsAdapter<AlbumComposition>(
    lifecycleOwner,
    recyclerView,
    selectedCompositions,
    onCompositionClickListener,
    onLongClickListener,
    iconClickListener,
    menuClickListener,
    SimpleDiffItemCallback(::areSourcesTheSame, ::getChangePayload)
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CompositionViewHolder<AlbumComposition> {
        return AlbumCompositionViewHolder(
            parent,
            onCompositionClickListener,
            onLongClickListener,
            iconClickListener,
            menuClickListener
        )
    }
}

private fun areSourcesTheSame(first: AlbumComposition, second: AlbumComposition): Boolean {
    return CompositionHelper.areSourcesTheSame(first, second)
            && first.discNumber == second.discNumber
            && first.trackNumber == second.trackNumber
}

private fun getChangePayload(first: AlbumComposition, second: AlbumComposition): List<Any> {
    val payloads = CompositionHelper.getChangePayload(first, second)
    if (first.discNumber != second.discNumber) {
        payloads.add(Payloads.DISC_NUMBER)
    }
    if (first.trackNumber != second.trackNumber) {
        payloads.add(Payloads.TRACK_NUMBER)
    }
    return payloads
}