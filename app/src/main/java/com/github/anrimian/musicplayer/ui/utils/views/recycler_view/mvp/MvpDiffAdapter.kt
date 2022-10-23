package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

abstract class MvpDiffAdapter<T, VH : MvpDiffAdapter.MvpViewHolder>(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    diffCallback: DiffUtil.ItemCallback<T>,
): DiffListAdapter<T, VH>(recyclerView, diffCallback) {

    protected val viewHolders = HashSet<VH>()

    init {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                forEachHolder(MvpViewHolder::release)
            }
        })
    }

    @CallSuper
    override fun onBindViewHolder(holder: VH, position: Int) {
        if (viewHolders.contains(holder)) {
            holder.release()
        }
        viewHolders.add(holder)
    }

    @CallSuper
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        viewHolders.remove(holder)
        holder.release()
    }

    protected fun forEachHolder(action: (VH) -> Unit) {
        viewHolders.forEach(action)
    }

    abstract class MvpViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        open fun release() {}
    }
}
