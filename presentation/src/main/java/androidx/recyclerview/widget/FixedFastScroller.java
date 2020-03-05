package androidx.recyclerview.widget;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

//thumb height problems on large lists:
//https://stackoverflow.com/questions/47846873/recyclerview-fast-scroll-thumb-height-too-small-for-large-data-set/48157813#48157813
@SuppressLint("VisibleForTests")
public class FixedFastScroller extends FastScroller{
    FixedFastScroller(RecyclerView recyclerView, StateListDrawable verticalThumbDrawable, Drawable verticalTrackDrawable, StateListDrawable horizontalThumbDrawable, Drawable horizontalTrackDrawable, int defaultWidth, int scrollbarMinimumRange, int margin) {
        super(recyclerView, verticalThumbDrawable, verticalTrackDrawable, horizontalThumbDrawable, horizontalTrackDrawable, defaultWidth, scrollbarMinimumRange, margin);
    }

    @Override
    void updateScrollPosition(int offsetX, int offsetY) {
        super.updateScrollPosition(offsetX, offsetY);
    }
}
