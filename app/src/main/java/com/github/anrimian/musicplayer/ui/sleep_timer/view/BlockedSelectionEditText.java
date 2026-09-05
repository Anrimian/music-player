package com.github.anrimian.musicplayer.ui.sleep_timer.view;

import android.content.Context;
import android.util.AttributeSet;
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SafeArrowKeyMovementMethod;

public class BlockedSelectionEditText extends
        androidx.appcompat.widget.AppCompatEditText {

    public BlockedSelectionEditText (Context context) {
        super(context);
        setMovementMethod(SafeArrowKeyMovementMethod.INSTANCE);
    }

    public BlockedSelectionEditText (Context context, AttributeSet attrs) {
        super(context, attrs);
        setMovementMethod(SafeArrowKeyMovementMethod.INSTANCE);
    }

    public BlockedSelectionEditText (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setMovementMethod(SafeArrowKeyMovementMethod.INSTANCE);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //on selection move cursor to end of text
        int length = this.length();
        if (selStart != length || selEnd != length) {
            post(() -> setSelection(length));
        }
    }

}