package com.github.anrimian.simplemusicplayer.utils.recycler_view.endless_scrolling;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public abstract class HeaderFooterRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = Integer.MAX_VALUE;
    private static final int TYPE_FOOTER = Integer.MIN_VALUE;
    protected static final int TYPE_ITEM = 0;

    private View header;
    private View footer;

    private boolean isFooterVisible = true;

    private boolean horizontal = false;

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        if (type == TYPE_HEADER) {
            return new HeaderFooterViewHolder(header);
        } else if (type == TYPE_FOOTER){
            return new HeaderFooterViewHolder(footer);
        }
        return createVH(viewGroup, type);
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder vh, int position) {
        if (!(position == 0 && hasHeader()) && !(position >= getCount() + (hasHeader() ? 1 : 0) && hasFooter())) {
            bindVH((T) vh, position - (hasHeader() ? 1 : 0));
        }
    }

    @Override
    public int getItemCount() {
        return getCount() + (hasHeader() ? 1 : 0) + (hasFooter() ? 1 : 0);
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public abstract T createVH(ViewGroup parent, int type);

    public abstract void bindVH(final T holder, int position);

    public abstract int getCount();

    private boolean hasHeader() {
        return header != null;
    }

    private boolean hasFooter() {
        return footer != null && isFooterVisible;
    }

    @Override
    public final int getItemViewType(int position) {
        if (position == 0 && hasHeader()) {
            return TYPE_HEADER;
        } else if (position >= getCount() + (hasHeader() ? 1 : 0)) {
            return TYPE_FOOTER;
        }
        return getItemType(position - (hasHeader() ? 1 : 0));
    }

    protected int getItemType(int position) {
        return 0;
    }


    public void addHeader(View header) {
        this.header = wrapView(header);
        notifyItemInserted(0);
    }

    public void removeHeader() {
        notifyItemRemoved(0);
        header = null;
    }

    public void addFooter(View footer) {
        this.footer = wrapView(footer);
        notifyItemInserted(getItemCount());
    }

    public void removeFooter() {
        notifyItemRemoved(getItemCount());
        footer = null;
    }

    public View getHeader() {
        return header;
    }

    public View getFooter() {
        return footer;
    }

    public void notifyItemRangeWithFooterInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart + 1, itemCount);
        isFooterVisible = true;
        notifyItemInserted(getItemCount());
    }

    public void notifyItemRangeWithFooterRemoved(int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);
        notifyItemRemoved(getItemCount());//this first -> wrong animation
        isFooterVisible = false;//this first -> crash//TODO fix footer animation
    }

    private View wrapView(View view) {
        FrameLayout frameLayout = new FrameLayout(view.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (horizontal) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        frameLayout.setLayoutParams(params);
        frameLayout.addView(view);
        return frameLayout;
    }

    public static class HeaderFooterViewHolder extends RecyclerView.ViewHolder {

        HeaderFooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
