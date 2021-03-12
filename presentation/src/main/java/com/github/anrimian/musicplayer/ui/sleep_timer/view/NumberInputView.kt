package com.github.anrimian.musicplayer.ui.sleep_timer.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection


class NumberInputView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    var textListener: ((Int) -> Unit)? = null

    private var pos = 0

    init {
//        setOnClickListener { v ->
//            val imm: InputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(v, 0)
//        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        isFocusableInTouchMode = true
    }

//    override fun onCheckIsTextEditor(): Boolean {
//        return true
//    }


    override fun onSelectionChanged(start: Int, end: Int) {
        val text = text
        if (text != null) {
            if (start != text.length || end != text.length) {
                return
            }
        }
        super.onSelectionChanged(start, end)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER
        return CustomInputConnection(this) {  }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            val keyNumber = getKeyNumber(event.keyCode)
            if (keyNumber != -1 || event.keyCode == KeyEvent.KEYCODE_DEL) {
                return false
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun getKeyNumber(keyCode: Int): Int {
        return when(keyCode) {
            KeyEvent.KEYCODE_0 -> 0
            KeyEvent.KEYCODE_1 -> 1
            KeyEvent.KEYCODE_2 -> 2
            KeyEvent.KEYCODE_3 -> 3
            KeyEvent.KEYCODE_4 -> 4
            KeyEvent.KEYCODE_5 -> 5
            KeyEvent.KEYCODE_6 -> 6
            KeyEvent.KEYCODE_7 -> 7
            KeyEvent.KEYCODE_8 -> 8
            KeyEvent.KEYCODE_9 -> 9
            else -> -1
        }
    }

    private class CustomInputConnection(
            view: View,
            private val onDelButtonClickListener: () -> Unit,
    ) : BaseInputConnection(view, true) {

        init {
            this.setSelection(0, 0)
        }

//        override fun getEditable(): Editable {
//            return Editable {
//
//            }
//        }

        override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
            return super.setComposingText(text, newCursorPosition)
        }

        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
            return super.commitText(text, newCursorPosition)
        }

        override fun deleteSurroundingText(leftLength: Int, rightLength: Int): Boolean {
            // Android SDK 16+ doesn't send key events for backspace but calls this method
            onDelButtonClickListener()
            return super.deleteSurroundingText(leftLength, rightLength)
        }
    }
}