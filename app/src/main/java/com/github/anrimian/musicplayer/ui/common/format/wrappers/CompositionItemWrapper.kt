package com.github.anrimian.musicplayer.ui.common.format.wrappers

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder
import com.github.anrimian.musicplayer.ui.common.format.getHighlightColor
import com.github.anrimian.musicplayer.ui.common.format.showFileSyncState
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.getHighlightAnimator
import com.github.anrimian.musicplayer.ui.utils.views.progress_bar.ProgressView

open class CompositionItemWrapper<T: Composition>(
    itemView: View,
    onIconClickListener: (T) -> Unit,
    onClickListener: (T) -> Unit
) {
    private val tvMusicName: TextView = itemView.findViewById(R.id.tv_composition_name)
    private val tvAdditionalInfo: TextView = itemView.findViewById(R.id.tv_additional_info)
    private val clickableItem: FrameLayout = itemView.findViewById(R.id.clickable_item)
    private val divider: View = itemView.findViewById(R.id.divider)
    private val ivPlay: ImageView = itemView.findViewById(R.id.iv_play)
    private val ivMusicIcon: ImageView = itemView.findViewById(R.id.ivMusicIcon)
    private val btnActionsMenu: ImageView = itemView.findViewById(R.id.btnActionsMenu)
    private val iconClickableArea: View = itemView.findViewById(R.id.icon_clickable_area)
    private val pvFileState: ProgressView = itemView.findViewById(R.id.pvFileState)

    private val itemBackgroundWrapper = ItemBackgroundWrapper(itemView, clickableItem)

    protected lateinit var composition: T

    private var showCovers = false
    private var isCurrent = false
    private var isDragging = false
    private var isSwiping = false

    private var fileSyncState: FileSyncState? = null

    init {
        iconClickableArea.setOnClickListener { onIconClickListener(composition) }
        clickableItem.setOnClickListener { onClickListener(composition) }
    }

    fun bind(composition: T, showCovers: Boolean) {
        this.composition = composition
        this.showCovers = showCovers

        showCompositionName()
        showAdditionalInfo()
        showCorrupted()
        showCompositionImage(showCovers)
        showAsPlaying(isPlaying = false, animate = false)
        updateFileSyncState()
    }

    fun update(composition: T, payloads: List<*>) {
        this.composition = composition
        for (payload in payloads) {
            if (payload is List<*>) {
                update(composition, payload)
            }
            if (payload == Payloads.FILE_NAME || payload == Payloads.TITLE) {
                showCompositionName()
            }
            if (payload == Payloads.ARTIST || payload == Payloads.DURATION) {
                showAdditionalInfo()
            }
            if (payload == Payloads.DATE_MODIFIED
                || payload == Payloads.SIZE
                || payload == Payloads.FILE_EXISTS
                || payload == Payloads.COVER_MODIFY_TIME) {
                showCompositionImage(showCovers)
            }
            if (payload == Payloads.CORRUPTED) {
                showCorrupted()
                showAdditionalInfo()
            }
            if (payload == Payloads.FILE_EXISTS) {
                updateFileSyncState()
            }
            onUpdate(payload)
        }
    }

    fun showCompositionImage(showCovers: Boolean) {
        if (showCovers) {
            Components.getAppComponent()
                .imageLoader()
                .displayImage(ivMusicIcon, composition, this::onCoverImageLoadFinished)
        } else {
            ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder_simple)
            ivMusicIcon.setColorFilter(Color.TRANSPARENT)
        }
    }

    fun release() {
        Components.getAppComponent().imageLoader().clearImage(ivMusicIcon)
    }

    fun showAsDraggingItem(dragging: Boolean) {
        if (isDragging != dragging) {
            isDragging = dragging
            ViewUtils.animateVisibility(divider, if (dragging) View.INVISIBLE else View.VISIBLE)
            if (!dragging && isCurrent) {
                showAsCurrentCompositionInternal(true)
            } else {
                showAsDragging(dragging)
            }
        }
    }

    fun showAsSwipingItem(swipeOffset: Float) {
        val swiping = swipeOffset > 0.0f
        if (isSwiping != swiping) {
            isSwiping = swiping
            val swipedCorners = getResources().getDimension(R.dimen.swiped_item_corners)
            val from = if (swiping) 0f else swipedCorners
            val to = if (swiping) swipedCorners else 0f
            val duration = getResources().getInteger(R.integer.swiped_item_animation_time)
            itemBackgroundWrapper.animateItemDrawableCorners(from, to, duration)
        }
    }

    fun showAsCurrentComposition(isCurrent: Boolean) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent
            showAsCurrentCompositionInternal(isCurrent)
        }
    }

    fun showAsPlaying(isPlaying: Boolean, animate: Boolean) {
        AndroidUtils.setAnimatedVectorDrawable(
            ivPlay,
            if (isPlaying) R.drawable.anim_play_to_pause else R.drawable.anim_pause_to_play,
            animate
        )
    }

    fun showStateColor(@ColorInt color: Int, animate: Boolean) {
        itemBackgroundWrapper.showStateColor(color, animate)
    }

    fun runHighlightAnimation() {
        getHighlightAnimator(
            getContext().colorFromAttr(R.attr.listItemBackground),
            getContext().getHighlightColor(),
            itemBackgroundWrapper::setBackgroundColor
        ).start()
    }

    fun showFileSyncState(fileSyncState: FileSyncState?) {
        this.fileSyncState = fileSyncState
        updateFileSyncState()
    }

    protected fun showAdditionalInfo() {
        val sb: SpannableStringBuilder = DescriptionSpannableStringBuilder(getContext())
        getAdditionalInfo(sb)
        tvAdditionalInfo.text = sb
    }

    protected open fun getAdditionalInfo(sb: SpannableStringBuilder) {
        sb.append(FormatUtils.formatCompositionAuthor(composition, getContext()))
        sb.append(FormatUtils.formatMilliseconds(composition.duration))
        val corruptionHint = getCorruptionTypeHint(composition)
        if (corruptionHint != null) {
            sb.append(corruptionHint)
            val start = sb.length - corruptionHint.length
            val end = sb.length
            val fcs = ForegroundColorSpan(getContext().colorFromAttr(R.attr.colorError))
            sb.setSpan(fcs, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
    }

    protected open fun getContext(): Context = clickableItem.context

    protected open fun onUpdate(payload: Any?) {}

    private fun onCoverImageLoadFinished(loaded: Boolean) {
        var tint = Color.TRANSPARENT
        if (loaded) {
            tint = ContextCompat.getColor(getContext(), R.color.cover_dark_foreground)
        }
        ivMusicIcon.setColorFilter(tint)
    }

    private fun showAsCurrentCompositionInternal(isPlaying: Boolean) {
        if (!isPlaying && isDragging) {
            showAsDragging(true)
            return
        }
        val endColor = ColorFormatUtils.getPlayingCompositionColor(
            getContext(),
            if (isPlaying) 25 else 0
        )
        itemBackgroundWrapper.showStateColor(endColor, true)
    }

    private fun showCompositionName() {
        val compositionName = CompositionHelper.formatCompositionName(composition)
        tvMusicName.text = compositionName
        clickableItem.contentDescription = compositionName
        iconClickableArea.contentDescription = compositionName
    }

    private fun showCorrupted() {
        val alpha = if (composition.corruptionType == null) 1f else 0.5f
        tvMusicName.alpha = alpha
        tvAdditionalInfo.alpha = alpha
        ivMusicIcon.alpha = alpha
        ivPlay.alpha = alpha
        btnActionsMenu.alpha = alpha
        pvFileState.alpha = alpha
    }

    private fun showAsDragging(dragging: Boolean) {
        val endColor = ColorFormatUtils.getItemDragColor(getContext(), if (dragging) 20 else 0)
        itemBackgroundWrapper.showStateColor(endColor, true)
    }

    private fun getCorruptionTypeHint(composition: Composition): String? {
        val corruptionType = composition.corruptionType ?: return null
        return when (corruptionType) {
            CorruptionType.UNSUPPORTED -> getContext().getString(R.string.unsupported_format_hint)
            CorruptionType.NOT_FOUND -> getContext().getString(R.string.file_not_found)
            CorruptionType.SOURCE_NOT_FOUND -> getContext().getString(R.string.file_source_not_found)
            CorruptionType.TOO_LARGE_SOURCE -> getContext().getString(R.string.file_is_too_large)
            CorruptionType.FILE_IS_CORRUPTED -> getContext().getString(R.string.file_is_corrupted)
            CorruptionType.FILE_READ_TIMEOUT -> getContext().getString(R.string.file_read_timeout)
            else -> getContext().getString(R.string.unknown_play_error)
        }
    }

    private fun updateFileSyncState() {
        //TODO play queue small-land placement
        showFileSyncState(
            fileSyncState,
            CompositionHelper.isCompositionFileRemote(composition),
            pvFileState
        )
    }

    private fun getResources() = getContext().resources
}