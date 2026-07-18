package com.github.anrimian.musicplayer.ui.utils.views.text_view;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.BaseMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * A movement method that traverses links in the text buffer and fires clicks. Unlike
 * {@link LinkMovementMethod}, this will not consume touch events outside {@link ClickableSpan}s.
 */
public class ClickableMovementMethod extends BaseMovementMethod {

    private static ClickableMovementMethod sInstance;

    public static ClickableMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new ClickableMovementMethod();
        }
        return sInstance;
    }

    public static void attachMovementMethod(TextView textView) {
        textView.setMovementMethod(ClickableMovementMethod.getInstance());
        // Reset for TextView.fixFocusableAndClickableSettings(). We don't want View.onTouchEvent()
        // to consume touch events.
        textView.setClickable(false);
        textView.setLongClickable(false);
    }

    @Override
    public boolean canSelectArbitrarily() {
        return false;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {

            int x = (int) event.getX();
            int y = (int) event.getY();
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            ClickableSpan[] links;
            if (y < 0 || y > layout.getHeight()) {
                links = null;
            } else {
                int line = layout.getLineForVertical(y);
                if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) {
                    links = null;
                } else {
                    int off = layout.getOffsetForHorizontal(line, x);
                    links = buffer.getSpans(off, off, ClickableSpan.class);
                }
            }

            if (links != null && links.length > 0) {
                if (action == MotionEvent.ACTION_UP) {
                    links[0].onClick(widget);
                } else {
                    Selection.setSelection(buffer, buffer.getSpanStart(links[0]),
                            buffer.getSpanEnd(links[0]));
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return false;
    }

    @Override
    public void initialize(TextView widget, Spannable text) {
        Selection.removeSelection(text);
    }
}