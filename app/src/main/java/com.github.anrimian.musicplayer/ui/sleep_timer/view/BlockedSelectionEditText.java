package com.github.anrimian.musicplayer.ui.sleep_timer.view;

import android.content.Context;
import android.util.AttributeSet;

public class BlockedSelectionEditText extends
        androidx.appcompat.widget.AppCompatEditText {

    public BlockedSelectionEditText (Context context) {
        super(context);
    }

    public BlockedSelectionEditText (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlockedSelectionEditText (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //on selection move cursor to end of text
        setSelection(this.length());
    }

}