package com.github.anrimian.musicplayer.ui.common.dialogs

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.StyleRes
import com.github.anrimian.musicplayer.R

class AppBottomSheetDialog(
    context: Context,
    @StyleRes theme: Int
): com.google.android.material.bottomsheet.BottomSheetDialog(context, theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val width = context.resources.getDimensionPixelSize(R.dimen.bottom_sheet_width)
        window?.setLayout(width, MATCH_PARENT)
    }
}