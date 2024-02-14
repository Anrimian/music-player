package com.github.anrimian.musicplayer.ui.settings.themes.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme

class ThemesAdapter(
    private val themes: Array<AppTheme>,
    private var currentTheme: AppTheme,
    private val themeClickListener: (AppTheme) -> Unit
) : RecyclerView.Adapter<ThemesViewHolder>() {

    private val viewHolders: MutableSet<ThemesViewHolder> = HashSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemesViewHolder {
        return ThemesViewHolder(parent, themeClickListener)
    }

    override fun onBindViewHolder(holder: ThemesViewHolder, position: Int) {
        viewHolders.add(holder)
        val theme = themes[position]
        holder.bind(theme, theme == currentTheme)
    }

    override fun getItemCount() = themes.size

    override fun onViewRecycled(holder: ThemesViewHolder) {
        super.onViewRecycled(holder)
        viewHolders.remove(holder)
    }

    fun setCurrentTheme(currentTheme: AppTheme) {
        this.currentTheme = currentTheme
        for (holder in viewHolders) {
            holder.setSelected(holder.getAppTheme() == currentTheme)
        }
    }
}