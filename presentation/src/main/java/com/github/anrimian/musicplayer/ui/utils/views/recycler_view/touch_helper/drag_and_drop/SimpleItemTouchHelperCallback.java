package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.github.anrimian.musicplayer.R;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ViewUtils;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.LAYER_TYPE_SOFTWARE;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{

    private OnMovedListener onMovedListener;
    private OnStartDragListener onStartDragListener;
    private OnEndDragListener onEndDragListener;

    private boolean horizontalDrag;

    public SimpleItemTouchHelperCallback() {
        this(false);
    }

    public SimpleItemTouchHelperCallback(boolean horizontalDrag) {
        this.horizontalDrag = horizontalDrag;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (onStartDragListener != null && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            onStartDragListener.onStartDrag();
        }
        setIsDragging(viewHolder, true);
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        if (onEndDragListener != null) {
            onEndDragListener.onEndDrag();
        }
        setIsDragging(viewHolder, false);
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        if (onMovedListener != null) {
            onMovedListener.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int directions = ItemTouchHelper.DOWN | ItemTouchHelper.UP;
        if (horizontalDrag) {
            directions |= ItemTouchHelper.START | ItemTouchHelper.END;
        }
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, directions);
    }

//    private Paint paint = new Paint();

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        if (isCurrentlyActive) {
//            paint.setColor(Color.RED);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//            Point shadowSize = new Point();
//            Point shadowTouchPoint = new Point();
//            MyDragShadowBuilder builder = new MyDragShadowBuilder(viewHolder.itemView);
//            builder.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
//            builder.onDrawShadow(c);

//            View itemView = viewHolder.itemView;
//            float x = itemView.getX();
//            float y = itemView.getY();
//            int height = itemView.getHeight();
//            int width = itemView.getWidth();
//            paint.setShadowLayer(10,50,50, Color.BLACK);
//            itemView.setElevation(20f);
//            c.drawRect(x, y+height, x+width, y+height + 30, paint);

        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder,
                                float dX,
                                float dY,
                                int actionState,
                                boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    public void setOnMovedListener(OnMovedListener onMovedListener) {
        this.onMovedListener = onMovedListener;
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public void setOnEndDragListener(OnEndDragListener onEndDragListener) {
        this.onEndDragListener = onEndDragListener;
    }

    private void setIsDragging(RecyclerView.ViewHolder viewHolder, boolean dragging) {
        if (viewHolder == null) {
            return;
        }
        if (viewHolder instanceof DragListener) {
            ((DragListener) viewHolder).onDragStateChanged(dragging);
        }
        float baseElevation = dragging? 0f: 8f;
        float dragElevation = dragging? 8f: 0f;
        ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(viewHolder.itemView,//blinking on close
                "elevation",
                baseElevation,
                dragElevation);
        elevationAnimator.setDuration(DEFAULT_DRAG_ANIMATION_DURATION);
        elevationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                Log.d("KEK", "onAnimationUpdate: " + animation.getAnimatedValue());
            }
        });
        elevationAnimator.start();

//        if (viewHolder != null) {
//            float scale = dragging ? 1.05f : 1f;
////            ScalingLayoutOutlineProvider provider = null;
////            if (dragging) {
////                provider = new ScalingLayoutOutlineProvider();
////            }
//            if (dragging) {
//                viewHolder.itemView.setBackground(generateBackgroundWithShadow(viewHolder.itemView,
////                        R.color.light_primary_color,
////                    R.dimen.radius_corner,
//                        R.color.secondary_button_color,
////                    R.dimen.button_expand_size,
//                        Gravity.BOTTOM));
//            } else {
//                viewHolder.itemView.setBackground(null);
//            }
////            viewHolder.itemView.setOutlineProvider(provider);
//            viewHolder.itemView.animate()
//                    .setDuration(150)
//                    .scaleX(scale)
//                    .scaleY(scale)
//                    .start();
//        }
    }

    public static Drawable generateBackgroundWithShadow(View view,
//                                                        @ColorRes int backgroundColor,
//                                                        @DimenRes int cornerRadius,
                                                        @ColorRes int shadowColor,
//                                                        @DimenRes int elevation,
                                                        int shadowGravity) {
        float cornerRadiusValue = 6;//view.getContext().getResources().getDimension(cornerRadius);
        int elevationValue = 12;//(int) view.getContext().getResources().getDimension(elevation);
        int shadowColorValue = ContextCompat.getColor(view.getContext(),shadowColor);
        int backgroundColorValue = Color.WHITE;//ContextCompat.getColor(view.getContext(),backgroundColor);

        float[] outerRadius = {cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
                cornerRadiusValue, cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
                cornerRadiusValue};

        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setShadowLayer(cornerRadiusValue, 0, 0, 0);

        Rect shapeDrawablePadding = new Rect();
        shapeDrawablePadding.left = elevationValue;
        shapeDrawablePadding.right = elevationValue;

        int DY;
        switch (shadowGravity) {
            case Gravity.CENTER:
                shapeDrawablePadding.top = elevationValue;
                shapeDrawablePadding.bottom = elevationValue;
                DY = 0;
                break;
            case Gravity.TOP:
                shapeDrawablePadding.top = elevationValue*2;
                shapeDrawablePadding.bottom = elevationValue;
                DY = -1*elevationValue/3;
                break;
            default:
            case Gravity.BOTTOM:
                shapeDrawablePadding.top = elevationValue;
                shapeDrawablePadding.bottom = elevationValue*2;
                DY = elevationValue/3;
                break;
        }

        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.setPadding(shapeDrawablePadding);

        shapeDrawable.getPaint().setColor(backgroundColorValue);
        shapeDrawable.getPaint().setShadowLayer(cornerRadiusValue/3, 0, DY, shadowColorValue);

        view.setLayerType(LAYER_TYPE_SOFTWARE, shapeDrawable.getPaint());

        shapeDrawable.setShape(new RoundRectShape(outerRadius, null, null));

        LayerDrawable drawable = new LayerDrawable(new Drawable[]{shapeDrawable});
        drawable.setLayerInset(0, elevationValue, elevationValue*2, elevationValue, elevationValue*2);

        return drawable;

    }

    public interface OnMovedListener {
        void onItemMoved(int from, int to);
    }

    public interface OnStartDragListener {
        void onStartDrag();
    }

    public interface OnEndDragListener {
        void onEndDrag();
    }

}
