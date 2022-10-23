package com.github.anrimian.musicplayer.ui.common.format.wrappers;

import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.CORRUPTED;
import static com.github.anrimian.musicplayer.domain.Payloads.DATE_MODIFIED;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.FILE_EXISTS;
import static com.github.anrimian.musicplayer.domain.Payloads.FILE_NAME;
import static com.github.anrimian.musicplayer.domain.Payloads.SIZE;
import static com.github.anrimian.musicplayer.domain.Payloads.TITLE;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getItemDragColor;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.animateItemDrawableCorners;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateItemDrawableColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.github.anrimian.filesync.models.state.file.FileSyncState;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtilsKt;
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ViewUtilsKt;
import com.github.anrimian.musicplayer.ui.utils.views.progress_bar.ProgressView;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable;

import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class CompositionItemWrapper {

    private final TextView tvMusicName;
    private final TextView tvAdditionalInfo;
    private final FrameLayout clickableItem;
    private final View divider;
    private final ImageView ivPlay;
    private final ImageView ivMusicIcon;
    private final ImageView btnActionsMenu;
    private final View iconClickableArea;
    private final ProgressView pvFileState;

    private final ItemDrawable backgroundDrawable = new ItemDrawable();
    private final ItemDrawable stateDrawable = new ItemDrawable();
    private final ItemDrawable rippleMaskDrawable = new ItemDrawable();

    private Composition composition;
    private boolean showCovers;

    private boolean isCurrent;
    private boolean isDragging;
    private boolean isSwiping;

    private Disposable syncStateDisposable;
    private FileSyncState fileSyncState;

    public CompositionItemWrapper(View itemView,
                                  Callback<Composition> onIconClickListener,
                                  Callback<Composition> onClickListener) {
        tvMusicName = itemView.findViewById(R.id.tv_composition_name);
        tvAdditionalInfo = itemView.findViewById(R.id.tv_additional_info);
        clickableItem = itemView.findViewById(R.id.clickable_item);
        ivPlay = itemView.findViewById(R.id.iv_play);
        ivMusicIcon = itemView.findViewById(R.id.ivMusicIcon);
        divider = itemView.findViewById(R.id.divider);
        btnActionsMenu = itemView.findViewById(R.id.btnActionsMenu);
        iconClickableArea = itemView.findViewById(R.id.icon_clickable_area);
        pvFileState = itemView.findViewById(R.id.pvFileState);

        iconClickableArea.setOnClickListener(v -> onIconClickListener.call(composition));
        clickableItem.setOnClickListener(v -> onClickListener.call(composition));

        backgroundDrawable.setColor(getColorFromAttr(getContext(), R.attr.listItemBackground));
        itemView.setBackground(backgroundDrawable);
        stateDrawable.setColor(Color.TRANSPARENT);
        clickableItem.setBackground(stateDrawable);
        clickableItem.setForeground(new RippleDrawable(
                ColorStateList.valueOf(getColorFromAttr(getContext(), android.R.attr.colorControlHighlight)),
                null,
                rippleMaskDrawable));
    }

    public void bind(Composition composition, boolean showCovers) {
        this.composition = composition;
        this.showCovers = showCovers;

        showCompositionName();
        showAdditionalInfo();
        showCorrupted();
        showCompositionImage(showCovers);

        showAsPlaying(false, false);

        showFileSyncState();
        subscribeOnFileSyncState();
    }

    public void update(Composition composition, List<?> payloads) {
        this.composition = composition;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                update(composition, (List<?>) payload);
            }
            if (payload == FILE_NAME || payload == TITLE) {
                showCompositionName();
            }
            if (payload == ARTIST || payload == DURATION) {
                showAdditionalInfo();
            }
            if (payload == DATE_MODIFIED
                    || payload == SIZE
                    || payload == FILE_EXISTS) {
                showCompositionImage(showCovers);
            }
            if (payload == CORRUPTED) {
                showCorrupted();
                showAdditionalInfo();
            }
            if (payload == FILE_EXISTS) {
                showFileSyncState();
            }
        }
    }

    public void showCompositionImage(boolean showCovers) {
        if (showCovers) {
            Components.getAppComponent().imageLoader().displayImage(ivMusicIcon,
                    composition,
                    this::onCoverImageLoadFinished);
        } else {
            ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder_simple);
            ivMusicIcon.setColorFilter(Color.TRANSPARENT);
        }
    }

    public void release() {
        Components.getAppComponent().imageLoader().clearImage(ivMusicIcon);
        if (syncStateDisposable != null) {
            syncStateDisposable.dispose();
            syncStateDisposable = null;
        }
    }

    public void showAsDraggingItem(boolean dragging) {
        if (this.isDragging != dragging) {
            this.isDragging = dragging;

            animateVisibility(divider, dragging? View.INVISIBLE: View.VISIBLE);
            if (!dragging && isCurrent) {
                showAsCurrentCompositionInternal(true);
            } else {
                showAsDragging(dragging);
            }
        }
    }

    public void showAsSwipingItem(float swipeOffset) {
        boolean swiping = swipeOffset > 0.0f;

        if (this.isSwiping != swiping) {
            this.isSwiping = swiping;

            float swipedCorners = getContext().getResources().getDimension(R.dimen.swiped_item_corners);
            float from = swiping? 0: swipedCorners;
            float to = swiping? swipedCorners: 0;
            int duration = getResources().getInteger(R.integer.swiped_item_animation_time);
            animateItemDrawableCorners(from, to, duration, backgroundDrawable, stateDrawable, rippleMaskDrawable);
        }
    }

    public void showAsCurrentComposition(boolean isCurrent) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent;
            showAsCurrentCompositionInternal(isCurrent);
        }
    }

    public void showAsPlaying(boolean isPlaying, boolean animate) {
        AndroidUtils.setAnimatedVectorDrawable(ivPlay,
                isPlaying? R.drawable.anim_play_to_pause: R.drawable.anim_pause_to_play,
                animate);
    }

    public void showStateColor(@ColorInt int color, boolean animate) {
        if (animate) {
            stateDrawable.setColor(color);
        } else {
            animateItemDrawableColor(stateDrawable, color);
        }
    }

    public void runHighlightAnimation() {
        ValueAnimator colorAnimator = ViewUtilsKt.getHighlightAnimator(
                getColorFromAttr(getContext(), R.attr.listItemBackground),
                FormatUtilsKt.getHighlightColor(getContext()));
        colorAnimator.addUpdateListener(animator ->
                backgroundDrawable.setColor((int) animator.getAnimatedValue())
        );
        colorAnimator.start();
    }

    private void onCoverImageLoadFinished(boolean loaded) {
        int tint = Color.TRANSPARENT;
        if (loaded) {
            tint = ContextCompat.getColor(getContext(), R.color.cover_dark_foreground);
        }
        ivMusicIcon.setColorFilter(tint);
    }

    private void showAsCurrentCompositionInternal(boolean isPlaying) {
        if (!isPlaying && isDragging) {
            showAsDragging(true);
            return;
        }
        int endColor = getPlayingCompositionColor(getContext(), isPlaying? 25: 0);
        animateItemDrawableColor(stateDrawable, endColor);
    }

    private void showCompositionName() {
        String compositionName = formatCompositionName(composition);
        tvMusicName.setText(compositionName);
        clickableItem.setContentDescription(compositionName);
        iconClickableArea.setContentDescription(compositionName);
    }

    private void showCorrupted() {
        float alpha = composition.getCorruptionType() == null? 1f: 0.5f;
        tvMusicName.setAlpha(alpha);
        tvAdditionalInfo.setAlpha(alpha);
        ivMusicIcon.setAlpha(alpha);
        ivPlay.setAlpha(alpha);
        btnActionsMenu.setAlpha(alpha);
        pvFileState.setAlpha(alpha);
    }

    private void showAsDragging(boolean dragging) {
        int endColor = getItemDragColor(getContext(), dragging? 20: 0);
        animateItemDrawableColor(stateDrawable, endColor);
    }

    private void showAdditionalInfo() {
        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(getContext());
        sb.append(formatCompositionAuthor(composition, getContext()));
        sb.append(formatMilliseconds(composition.getDuration()));
        String corruptionHint = getCorruptionTypeHint();
        if (corruptionHint != null) {
            sb.append(corruptionHint);
            int start = sb.length() - corruptionHint.length();
            int end = sb.length();

            ForegroundColorSpan fcs = new ForegroundColorSpan(getColorFromAttr(getContext(), R.attr.colorError));
            sb.setSpan(fcs, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        tvAdditionalInfo.setText(sb);
    }

    private String getCorruptionTypeHint() {
        CorruptionType corruptionType = composition.getCorruptionType();
        if (corruptionType == null) {
            return null;
        }
        switch (composition.getCorruptionType()) {
            case UNSUPPORTED: return getContext().getString(R.string.unsupported_format_hint);
            case NOT_FOUND: return getContext().getString(R.string.file_not_found);
            case SOURCE_NOT_FOUND: return getContext().getString(R.string.file_source_not_found);
            case TOO_LARGE_SOURCE: return getContext().getString(R.string.file_is_too_large);
            default: return null;
        }
    }

    private void subscribeOnFileSyncState() {
        if (syncStateDisposable != null) {
            syncStateDisposable.dispose();
        }
        syncStateDisposable = FormatUtilsKt.getFileSyncStateObservable(composition.getId())
                .doOnDispose(() -> {
                    pvFileState.clearIcon();
                    pvFileState.clearProgress();
                })
                .subscribe(this::showFileSyncState);
    }

    private void showFileSyncState(FileSyncState fileSyncState) {
        this.fileSyncState = fileSyncState;
        showFileSyncState();
    }

    private void showFileSyncState() {
        //TODO play queue small-land placement
        FormatUtilsKt.showFileSyncState(fileSyncState,
                CompositionHelper.isCompositionFileRemote(composition),
                pvFileState);
    }

    private Context getContext() {
        return clickableItem.getContext();
    }

    private Resources getResources() {
        return getContext().getResources();
    }
}
