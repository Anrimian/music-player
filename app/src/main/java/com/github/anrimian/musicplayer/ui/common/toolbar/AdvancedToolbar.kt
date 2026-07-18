package com.github.anrimian.musicplayer.ui.common.toolbar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.ActionMenuView
import androidx.fragment.app.FragmentActivity
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.PartialToolbarBinding
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow.showPopup
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.getParcelableExtra
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class AdvancedToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = PartialToolbarBinding.inflate(LayoutInflater.from(context), this)

    @ColorInt private var controlButtonColor = 0
    @ColorInt private var controlButtonActionModeColor = 0
    @ColorInt private var backgroundColor = 0
    @ColorInt private var backgroundActionModeColor = 0
    @ColorInt private var statusBarColor = 0
    @ColorInt private var statusBarActionModeColor = 0

    private val backgroundDrawable: ToolbarBackgroundDrawable

    private val navigationDrawable: DrawerArrowDrawable
    private var isNavigationDrawableStateInitialized = false
    private var isNavigationButtonBackModeLocked = false
    private var titleClickListener: OnClickListener? = null
    private var textChangeListener: ((String) -> Unit)? = null
    private var textConfirmListener: ((String) -> Unit)? = null
    private var actionModeExitListener: (() -> Unit)? = null
    private var searchModeExitListener: (() -> Unit)? = null

    private val searchModeSubject = BehaviorSubject.createDefault(false)
    private val searchModeLockedSubject = BehaviorSubject.createDefault(false)
    private val selectionModeSubject = BehaviorSubject.createDefault(false)

    private var isInSearchMode = false
    private var isInActionMode = false

    private var contentAlpha = 1f

    init {
        binding.etSearch.addTextChangedListener(SimpleTextWatcher(::onSearchTextChanged))
        binding.etSearch.setOnEditorActionListener(OnEditorActionListener(::onSearchTextViewAction))

        binding.tvSubtitle.visibility = GONE
        binding.etSearch.visibility = INVISIBLE
        binding.ivActionIcon.visibility = GONE
        binding.clSelectionMode.visibility = INVISIBLE
        binding.ivNavHint.visibility = INVISIBLE

        controlButtonColor = context.attrColor(R.attr.toolbarTextColorPrimary)
        controlButtonActionModeColor = context.attrColor(R.attr.actionModeTextColor)
        backgroundColor = context.attrColor(R.attr.colorPrimary)
        backgroundActionModeColor = context.attrColor(R.attr.actionModeBackgroundColor)
        statusBarColor = context.attrColor(android.R.attr.statusBarColor)
        statusBarActionModeColor = context.attrColor(R.attr.actionModeStatusBarColor).let { color ->
            if (color == -1) 0 else color//strange bug if color is white(0) - can't find get color from resource
        }

        backgroundDrawable = ToolbarBackgroundDrawable(backgroundColor, statusBarColor)
        background = backgroundDrawable

        navigationDrawable = DrawerArrowDrawable(context).apply {
            color = context.attrColor(R.attr.toolbarTextColorPrimary)
        }
        binding.ivNavigation.setImageDrawable(navigationDrawable)
        binding.ivNavigation.visibility = View.INVISIBLE
    }

    fun setNavigationButtonBackModeLocked(isLocked: Boolean) {
        isNavigationButtonBackModeLocked = isLocked
    }

    fun setNavigationButtonClickListener(onClick: () -> Unit) {
        binding.ivNavigation.setOnClickListener { onClick() }
    }

    fun setNavigationButtonBackClickListener(onClick: () -> Unit) {
        isNavigationButtonBackModeLocked = true
        setNavigationButtonProgressInternal(1f)
        binding.ivNavigation.setOnClickListener { onClick() }
    }

    fun setNavigationButtonProgress(progress: Float) {
        setNavigationButtonProgressInternal(progress)
    }

    fun setNavigationButtonMode(isBase: Boolean, animate: Boolean) {
        val endProgress = if (isBase) 0f else 1f // 0 for hamburger, 1 for arrow
        if (navigationDrawable.progress == endProgress) {
            initNavButtonIfNeeded()
            return
        }

        if (animate && isNavigationDrawableStateInitialized) {
            val animator = getControlButtonAnimatorToState(targetProgress = endProgress)
            animator.duration = AppConstants.Animation.TOOLBAR_ARROW_ANIMATION_TIME
            animator.start()
        } else {
            setNavigationButtonProgressInternal(endProgress)
        }
    }

    fun setup(configCallback: SetupConfig.() -> Unit): AdvancedToolbar {
        val config = SetupConfig(context, this, getTitle(), getSubtitle(), getSearchText())
        configCallback(config)
        setTitle(config.title)
        setSubtitle(config.subtitle)
        setupSearch(config.textChangeListener, config.searchExitListener, config.configSearchText)
        setSearchLocked(config.isSearchLocked)
        setupOptionsMenu(config.menuResId, config.menuListener)
        setTitleClickListener(config.titleClickListener)
        setupSelectionModeMenu(
            config.selectionMenuResId,
            config.selectionMenuListener,
            config.selectionMenuExitListener
        )
        return this
    }

    @SuppressLint("CheckResult")
    fun attachBackPressedCallback(activity: FragmentActivity) {
        val onBackPressedCallback = object : OnBackPressedCallback(isInSearchMode() || isInActiveSearchMode()) {
            override fun handleOnBackPressed() {
                if (isInActionMode()) {
                    invokeActionModeExit()
                    return
                }
                if (isInActiveSearchMode()) {
                    invokeSearchModeExit()
                    return
                }
            }
        }
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
        Observable.combineLatest(
            getActiveSearchModeObservable(),
            getSelectionModeObservable()
        ) { isSearchActive, isActionModeActive ->
            isSearchActive || isActionModeActive
        }.subscribe { isAnyModeActive -> onBackPressedCallback.isEnabled = isAnyModeActive }
    }

    fun setSearchModeEnabled(
        enabled: Boolean,
        showKeyboard: Boolean = true,
        jumpToState: Boolean = false
    ) {
        isInSearchMode = enabled
        searchModeSubject.onNext(enabled)
        binding.etSearch.visibility = if (enabled) VISIBLE else GONE
        binding.clTitleContainer.alpha = if (enabled || contentAlpha < 1f) 0f else 1f
        binding.acvMain.visibility = if (enabled) GONE else VISIBLE

        if (enabled || !isNavigationButtonBackModeLocked) {
            setNavigationButtonMode(
                isBase = !enabled,
                animate = !jumpToState
            )
        }

        if (enabled) {
            binding.etSearch.requestFocus()
            if (showKeyboard) {
                AndroidUtils.showKeyboard(binding.etSearch)
            }
        } else {
            binding.etSearch.text = null
            AndroidUtils.hideKeyboard(binding.etSearch)
        }
    }

    fun setupOptionsMenu(@MenuRes menuResId: Int, listener: ((MenuItem) -> Unit)?): Menu {
        val menuValidResId = if (menuResId == 0) R.menu.empty_stub_menu else menuResId
        val validListener = listener ?: {}
        return ActionMenuUtil.setupMenu(binding.acvMain, menuValidResId, validListener)
    }

    fun clearOptionsMenu() {
        ActionMenuUtil.setupMenu(binding.acvMain, R.menu.empty_stub_menu) { }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putBoolean(IN_SEARCH_MODE, isInSearchMode)
        bundle.putBoolean(IN_SELECTION_MODE, isInActionMode)
        bundle.putBoolean(IS_KEYBOARD_SHOWN, AndroidUtils.isKeyboardWasShown(binding.etSearch))
        return bundle
    }

    override fun onRestoreInstanceState(baseState: Parcelable) {
        var state: Parcelable? = baseState
        if (state is Bundle) {
            val bundle = state
            val isInSearchMode = bundle.getBoolean(IN_SEARCH_MODE)
            val isKeyboardShown = bundle.getBoolean(IS_KEYBOARD_SHOWN)
            setSearchModeEnabled(isInSearchMode, isKeyboardShown, true)

            val inSelectionMode = bundle.getBoolean(IN_SELECTION_MODE)
            setSelectionModeEnabled(inSelectionMode, false)
            state = bundle.getParcelableExtra("superState")
        }
        super.onRestoreInstanceState(state)
    }

    fun getTitle() = binding.tvToolbarTitle.text

    fun setTitle(@StringRes titleId: Int) {
        setTitle(context.getString(titleId))
    }

    fun setTitle(title: CharSequence?) {
        binding.tvToolbarTitle.visibility = if (TextUtils.isEmpty(title)) GONE else VISIBLE
        binding.tvToolbarTitle.text = title
        binding.flTitleArea.contentDescription = title
    }

    fun getSubtitle() = binding.tvSubtitle.text

    fun setSubtitle(@StringRes titleId: Int) {
        setSubtitle(context.getString(titleId))
    }

    fun setSubtitle(subtitle: CharSequence?) {
        binding.tvSubtitle.visibility = if (TextUtils.isEmpty(subtitle)) GONE else VISIBLE
        binding.tvSubtitle.text = subtitle
        if (!TextUtils.isEmpty(subtitle)) {
            binding.flTitleArea.contentDescription = getTitle().toString() + ", " + subtitle
        }
    }

    fun setTitleClickListener(listener: OnClickListener?) {
        this.titleClickListener = listener
        binding.ivActionIcon.visibility = if (listener == null) GONE else VISIBLE
        binding.flTitleArea.isEnabled = listener != null
        binding.flTitleArea.setOnClickListener(listener)
    }

    fun getTitleClickListener() = titleClickListener

    fun clearTitleMenu() {
        setTitleClickListener(null)
    }

    fun setupTitleMenu(@MenuRes menuResId: Int, listener: ((MenuItem) -> Unit)) {
        setTitleClickListener { v -> showPopup(v, menuResId, Gravity.BOTTOM, listener) }
    }

    fun isInSearchMode() = isInSearchMode

    fun isInActiveSearchMode() = isInSearchMode && !isSearchLocked()

    fun isInActionMode() = isInActionMode

    fun getActionMenuView(): ActionMenuView = binding.acvMain

    fun setupSearch(
        textChangeListener: ((String) -> Unit)?,
        exitListener: (() -> Unit)? = null,
        text: String? = null,
    ) {
        this.textChangeListener = textChangeListener
        this.searchModeExitListener = exitListener ?: { setSearchModeEnabled(false) }
        textConfirmListener = textChangeListener
        binding.etSearch.setText(text)
        setSearchModeEnabled(!TextUtils.isEmpty(text), showKeyboard = true, jumpToState = true)
    }

    fun invokeSearchModeExit() {
        searchModeExitListener?.invoke()
    }

    fun isSearchLocked() = !binding.etSearch.isEnabled

    fun setSearchLocked(locked: Boolean) {
        searchModeLockedSubject.onNext(locked)
        binding.etSearch.isEnabled = !locked
    }

    fun setStatusBarHeight(height: Int) {
        backgroundDrawable.setStatusBarHeight(height)
    }

    fun setStatusBarColor(@ColorInt color: Int) {
        backgroundDrawable.setStatusBarColor(color)
    }

    fun setToolbarBackgroundColor(@ColorInt color: Int) {
        backgroundDrawable.setColor(color)
    }

    fun getSearchText() = binding.etSearch.text.toString()

    fun getSearchModeObservable(): Observable<Boolean> = searchModeSubject

    fun getActiveSearchModeObservable(): Observable<Boolean> {
        return Observable.combineLatest(
            searchModeSubject,
            searchModeLockedSubject
        ) { isSearchModeEnabled, isSearchModeLocked -> isSearchModeEnabled && !isSearchModeLocked }
    }

    fun getSelectionModeObservable(): Observable<Boolean> = selectionModeSubject

    private fun getControlButtonAnimatorToState(targetProgress: Float): ValueAnimator {
        val startProgress = navigationDrawable.progress
        val animator = ValueAnimator.ofFloat(startProgress, targetProgress)
        animator.addUpdateListener { animation ->
            navigationDrawable.progress = animation.animatedValue as Float
        }
        return animator
    }

    private fun onSearchTextViewAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (textConfirmListener != null) {
            textConfirmListener!!.invoke(v.text.toString())
            return true
        }
        return false
    }

    private fun onSearchTextChanged(text: String) {
        textChangeListener?.invoke(text)
    }

    fun setControlButtonColor(@ColorInt color: Int) {
        navigationDrawable.color = color
    }

    fun setupSelectionModeMenu(
        @MenuRes menuResId: Int,
        listener: ((MenuItem) -> Unit)?,
        exitListener: (() -> Unit)?
    ) {
        val menuValidResId = if (menuResId == 0) R.menu.empty_stub_menu else menuResId
        val validListener = listener ?: {}
        ActionMenuUtil.setupMenu(binding.acvSelection, menuValidResId, validListener, 1)
        actionModeExitListener = exitListener
    }

    fun invokeActionModeExit() {
        actionModeExitListener?.invoke()
    }

    fun editActionMenu(callback: (Menu) -> Unit) {
        callback(binding.acvSelection.menu)
    }

    fun showSelectionMode(count: Int) {
        if (count == 0 && isInActionMode) {
            setSelectionModeEnabled(enabled = false, animate = true)
        }
        if (count > 0) {
            if (!isInActionMode) {
                setSelectionModeEnabled(enabled = true, animate = true)
            }
            binding.tvSelectionCount.text = count.toString()
        }
    }

    fun updateSelectionMenu(itemCallback: (MenuItem) -> Unit) {
        val menu = binding.acvSelection.menu
        for (i in 0 until menu.size()) {
            itemCallback(menu.getItem(i))
        }
    }

    fun setContentAlpha(alpha: Float) {
        contentAlpha = alpha
        binding.clTitleContainer.alpha = alpha
    }

    fun getToolbarModesViewGroup() = binding.flToolbarModes

    fun setNavigationButtonHintIcon(@DrawableRes iconRes: Int) {
        if (iconRes == -1) {
            binding.ivNavHint.visibility = INVISIBLE
            return
        }
        binding.ivNavHint.visibility = VISIBLE
        binding.ivNavHint.setImageResource(iconRes)
    }

    private fun setSelectionModeEnabled(enabled: Boolean, animate: Boolean) {
        isInActionMode = enabled
        selectionModeSubject.onNext(enabled)

        val currentNavProgress = navigationDrawable.progress
        val targetNavProgress = if (!isInSearchMode && !isNavigationButtonBackModeLocked) {
            if (enabled) 1f else 0f
        } else {
            currentNavProgress
        }

        val modeElementsVisibility = if (enabled) VISIBLE else INVISIBLE
        val anotherElementsVisibility = if (enabled) INVISIBLE else VISIBLE
        val startControlButtonColor = if (enabled) controlButtonColor else controlButtonActionModeColor
        val endControlButtonColor = if (enabled) controlButtonActionModeColor else controlButtonColor
        val startBackgroundColor = if (enabled) backgroundColor else backgroundActionModeColor
        val endBackgroundColor = if (enabled) backgroundActionModeColor else backgroundColor
        val startStatusBarColor = if (enabled) statusBarColor else statusBarActionModeColor
        val endStatusBarColor = if (enabled) statusBarActionModeColor else statusBarColor
        if (animate) {
            val duration = 300
            val mainAnimatorSet = AnimatorSet()
            mainAnimatorSet.duration = duration.toLong()
            mainAnimatorSet.play(getControlButtonAnimatorToState(targetNavProgress))
                .with(
                    ViewUtils.getColorAnimator(
                        startControlButtonColor,
                        endControlButtonColor
                    ) { color -> navigationDrawable.color = color }
                )
                .with(
                    ViewUtils.getColorAnimator(
                        startBackgroundColor,
                        endBackgroundColor,
                        backgroundDrawable::setColor
                    )
                )
                .with(
                    ViewUtils.getColorAnimator(
                        startBackgroundColor,
                        endBackgroundColor,
                        binding.ivNavHint::setBackgroundColor
                    )
                )
                .with(
                    ViewUtils.getColorAnimator(
                        startStatusBarColor,
                        endStatusBarColor,
                        backgroundDrawable::setStatusBarColor
                    )
                )
            val baseAnimators = ArrayList<Animator>()
            if (isInSearchMode) {
                baseAnimators.add(
                    ViewUtils.getVisibilityAnimator(
                        binding.etSearch,
                        anotherElementsVisibility
                    )
                )
            } else {
                baseAnimators.add(
                    ViewUtils.getVisibilityAnimator(
                        binding.clTitleContainer,
                        anotherElementsVisibility
                    )
                )
                baseAnimators.add(
                    ViewUtils.getVisibilityAnimator(
                        binding.acvMain, anotherElementsVisibility
                    )
                )
            }
            val baseElementsAnimator = AnimatorSet()
            baseElementsAnimator.playTogether(baseAnimators)
            baseElementsAnimator.duration = (duration / 2).toLong()
            val modeAnimators: MutableList<Animator> = ArrayList()
            modeAnimators.add(
                ViewUtils.getVisibilityAnimator(
                    binding.clSelectionMode,
                    modeElementsVisibility
                )
            )
            val modeElementsAnimator = AnimatorSet()
            modeElementsAnimator.playTogether(modeAnimators)
            modeElementsAnimator.duration = (duration / 2).toLong()
            val combinedAnimator = AnimatorSet()
            if (enabled) {
                combinedAnimator.play(baseElementsAnimator).before(modeElementsAnimator)
            } else {
                combinedAnimator.play(modeElementsAnimator).before(baseElementsAnimator)
            }
            val finalAnimatorSet = AnimatorSet()
            finalAnimatorSet.play(mainAnimatorSet).with(combinedAnimator)
            finalAnimatorSet.interpolator = if (enabled) DecelerateInterpolator() else AccelerateInterpolator()
            finalAnimatorSet.start()
        } else {
            setNavigationButtonProgressInternal(targetNavProgress)
            if (isInSearchMode) {
                binding.etSearch.visibility = anotherElementsVisibility
            } else {
                binding.clTitleContainer.visibility = anotherElementsVisibility
                binding.acvMain.visibility = anotherElementsVisibility
            }
            binding.clSelectionMode.visibility = modeElementsVisibility
            navigationDrawable.color = endControlButtonColor
            backgroundDrawable.setColor(endBackgroundColor)
            binding.ivNavHint.setBackgroundColor(endBackgroundColor)
            backgroundDrawable.setStatusBarColor(endStatusBarColor)
        }
    }

    private fun setNavigationButtonProgressInternal(progress: Float) {
        navigationDrawable.progress = progress
        initNavButtonIfNeeded()
    }

    private fun initNavButtonIfNeeded() {
        if (!isNavigationDrawableStateInitialized) {
            binding.ivNavigation.visibility = View.VISIBLE
        }
        isNavigationDrawableStateInitialized = true
    }

    class SetupConfig(
        private val context: Context,
        val toolbar: AdvancedToolbar,
        var title: CharSequence,
        var subtitle: CharSequence?,
        var configSearchText: String?
    ) {

        var textChangeListener: ((String) -> Unit)? = null
        private var textConfirmListener: ((String) -> Unit)? = null
        var searchExitListener: (() -> Unit)? = null
        var isSearchLocked = false

        @MenuRes
        var menuResId = 0
        var menuListener: ((MenuItem) -> Unit)? = null

        var titleClickListener: OnClickListener? = null

        @MenuRes
        var selectionMenuResId = 0
        var selectionMenuListener: ((MenuItem) -> Unit)? = null
        var selectionMenuExitListener: (() -> Unit)? = null

        fun setupSearch(
            textChangeListener: ((String) -> Unit)?,
            exitListener: (() -> Unit)? = null,
            text: String? = null,
        ) {
            this.textChangeListener = textChangeListener
            textConfirmListener = textChangeListener
            searchExitListener = exitListener
            configSearchText = text
        }

        fun setupOptionsMenu(@MenuRes menuResId: Int, listener: ((MenuItem) -> Unit)?) {
            this.menuResId = menuResId
            menuListener = listener
        }

        fun setupSelectionModeMenu(@MenuRes menuResId: Int,
                                   listener: ((MenuItem) -> Unit),
                                   exitListener: (() -> Unit)? = null) {
            selectionMenuResId = menuResId
            selectionMenuListener = listener
            selectionMenuExitListener = exitListener
        }

        fun setTitle(@StringRes titleId: Int) {
            setTitle(context.getString(titleId))
        }

        fun setTitle(title: String) {
            this.title = title
        }

        fun setSubtitle(subtitle: String?) {
            this.subtitle = subtitle
        }

        fun setSubtitle(@StringRes subtitleResId: Int) {
            this.subtitle = context.getString(subtitleResId)
        }
    }

    companion object {
        private const val IN_SEARCH_MODE = "in_search_mode"
        private const val IN_SELECTION_MODE = "in_selection_mode"
        private const val IS_KEYBOARD_SHOWN = "is_keyboard_shown"
    }

}