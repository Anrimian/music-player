package com.github.anrimian.musicplayer.ui.utils.views.text_view

import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.view.MotionEvent
import android.widget.TextView

object SafeArrowKeyMovementMethod : ArrowKeyMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        return try {
            super.onTouchEvent(widget, buffer, event)
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }
}
