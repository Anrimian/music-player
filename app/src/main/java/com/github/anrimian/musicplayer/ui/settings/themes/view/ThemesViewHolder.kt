package com.github.anrimian.musicplayer.ui.settings.themes.view

import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemThemeBinding
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class ThemesViewHolder(
    parent: ViewGroup,
    onThemeClickListener: (AppTheme) -> Unit
) : BaseViewHolder(parent, R.layout.item_theme) {

    private val binding = ItemThemeBinding.bind(itemView)

    private lateinit var appTheme: AppTheme

    init {
        binding.cardView.setOnClickListener { onThemeClickListener(appTheme) }
    }

    fun bind(appTheme: AppTheme, isSelected: Boolean) {
        this.appTheme = appTheme

        binding.fakeToolbar.setBackgroundResource(appTheme.primaryColorId)
        binding.fakeBackground.setBackgroundResource(appTheme.backgroundColorId)
        binding.fakeFab.setColorFilter(ContextCompat.getColor(getContext(), appTheme.accentColorId))

        (binding.cardView.foreground as RippleDrawable).setColor(
            ColorStateList.valueOf(ContextCompat.getColor(getContext(), appTheme.rippleColorId))
        )
        setSelected(isSelected)
    }

    fun setSelected(isSelected: Boolean) {
        binding.rbTheme.isChecked = isSelected
        binding.cardView.isClickable = !isSelected
    }

    fun getAppTheme() = appTheme
}