package com.github.anrimian.musicplayer.ui.library.folders.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.utils.FolderHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

/**
 * Created on 31.10.2017.
 */
private const val TYPE_MUSIC = 1
private const val TYPE_FILE = 2

class MusicFileSourceAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedItems: HashSet<FileSource>,
    private val selectedMoveItems: HashSet<FileSource>,
    private val onCompositionClickListener: (Int, CompositionFileSource) -> Unit,
    private val onFolderClickListener: (Int, FolderFileSource) -> Unit,
    private val onLongClickListener: (Int, FileSource) -> Unit,
    private val onFolderMenuClickListener: (View, FolderFileSource) -> Unit,
    private val compositionIconClickListener: (Composition) -> Unit,
    private val menuClickListener: (View, CompositionFileSource) -> Unit
) : MvpDiffAdapter<FileSource, FileViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(FolderHelper::areSourcesTheSame, FolderHelper::getChangePayload)
) {

    private var currentComposition: CurrentComposition? = null
    private var isCoversEnabled = false

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): FileViewHolder {
        return when (type) {
            TYPE_MUSIC -> MusicFileViewHolder(
                parent,
                onCompositionClickListener,
                onLongClickListener,
                compositionIconClickListener,
                menuClickListener
            )
            TYPE_FILE -> FolderViewHolder(
                parent,
                onFolderClickListener,
                onFolderMenuClickListener,
                onLongClickListener
            )
            else -> throw IllegalStateException("unexpected item type: $type")
        }
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val fileSource = getItem(position)
        val selected = selectedItems.contains(fileSource)
        holder.setSelected(selected)

        val selectedToMove = selectedMoveItems.contains(fileSource)
        holder.setSelectedToMove(selectedToMove)

        when (holder.itemViewType) {
            TYPE_MUSIC -> {
                val musicViewHolder = holder as MusicFileViewHolder
                val musicFileSource = fileSource as CompositionFileSource
                musicViewHolder.bind(musicFileSource, isCoversEnabled)
                musicViewHolder.showCurrentComposition(currentComposition, false)
            }
            TYPE_FILE -> {
                val folderViewHolder = holder as FolderViewHolder
                val folderFileSource = fileSource as FolderFileSource
                folderViewHolder.bind(folderFileSource)
            }
        }
    }

    override fun onBindViewHolder(
        holder: FileViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        val fileSource = getItem(position)
        when (holder.itemViewType) {
            TYPE_MUSIC -> {
                val musicViewHolder = holder as MusicFileViewHolder
                val musicFileSource = fileSource as CompositionFileSource
                musicViewHolder.update(musicFileSource, payloads)
            }
            TYPE_FILE -> {
                val folderViewHolder = holder as FolderViewHolder
                val folderFileSource = fileSource as FolderFileSource
                folderViewHolder.update(folderFileSource, payloads)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val source = getItem(position)
        return if (source is FolderFileSource) {
            TYPE_FILE
        } else {
            TYPE_MUSIC
        }
    }

    fun setItemSelected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_SELECTED)
    }

    fun setItemUnselected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_UNSELECTED)
    }

    fun setItemsSelected(selected: Boolean) {
        forEachHolder { holder ->
            holder.setSelected(selected)
        }
    }

    fun showCurrentComposition(currentComposition: CurrentComposition?) {
        this.currentComposition = currentComposition
        forEachHolder { holder ->
            if (holder is MusicFileViewHolder) {
                holder.showCurrentComposition(currentComposition, true)
            }
        }
    }

    fun setCoversEnabled(isCoversEnabled: Boolean) {
        this.isCoversEnabled = isCoversEnabled
        forEachHolder { holder ->
            if (holder is MusicFileViewHolder) {
                holder.setCoversVisible(isCoversEnabled)
            }
        }
    }

    fun updateItemsToMove() {
        forEachHolder { holder ->
            val selectedToMove = selectedMoveItems.contains(holder.fileSource)
            holder.setSelectedToMove(selectedToMove)
        }
    }

    fun highlightItem(position: Int) {
        for (holder in viewHolders) {
            if (holder is MusicFileViewHolder && holder.bindingAdapterPosition == position) {
                holder.runHighlightAnimation()
                return
            }
        }
    }
}