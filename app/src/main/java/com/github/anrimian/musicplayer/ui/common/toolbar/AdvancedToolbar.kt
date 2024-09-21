package com.github.anrimian.musicplayer.ui.common.toolbar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
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
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.ActionMenuView
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.PartialToolbarBinding
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow.showPopup
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentStackListener
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class AdvancedToolbar : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private lateinit var binding: PartialToolbarBinding

    private val stackChangeListener: FragmentStackListener = StackChangeListenerImpl()

    private lateinit var window: Window

    @ColorInt
    private var controlButtonColor = 0
    @ColorInt
    private var controlButtonActionModeColor = 0
    @ColorInt
    private var backgroundColor = 0
    @ColorInt
    private var backgroundActionModeColor = 0
    @ColorInt
    private var statusBarColor = 0
    @ColorInt
    private var statusBarActionModeColor = 0

    private lateinit var navigation: FragmentNavigation
    private lateinit var drawerArrowDrawable: DrawerArrowDrawable
    private lateinit var bottomSheetListener: (() -> Boolean)
    private var textChangeListener: ((String) -> Unit)? = null
    private var textConfirmListener: ((String) -> Unit)? = null

    private val searchModeSubject = BehaviorSubject.createDefault(false)
    private val selectionModeSubject = BehaviorSubject.createDefault(false)

    private var isContentVisible = false
    private var isInSearchMode = false
    private var isInActionMode = false

    fun init() {
        binding = PartialToolbarBinding.inflate(LayoutInflater.from(context), this)

        binding.etSearch.addTextChangedListener(SimpleTextWatcher(::onSearchTextChanged))
        binding.etSearch.setOnEditorActionListener(OnEditorActionListener(::onSearchTextViewAction))

        binding.etSearch.visibility = INVISIBLE
        binding.ivActionIcon.visibility = GONE
        binding.clSelectionMode.visibility = INVISIBLE

        controlButtonColor = context.attrColor(R.attr.toolbarTextColorPrimary)
        controlButtonActionModeColor = context.attrColor(R.attr.actionModeTextColor)
        backgroundColor = context.attrColor(R.attr.colorPrimary)
        backgroundActionModeColor = context.attrColor(R.attr.actionModeBackgroundColor)
    }

    fun setWindow(window: Window) {
        this.window = window
        statusBarColor = window.context.attrColor(android.R.attr.statusBarColor)
        statusBarActionModeColor = window.context.attrColor(R.attr.actionModeStatusBarColor)
    }

    fun setupWithNavigation(
        navigation: FragmentNavigation,
        drawerArrowDrawable: DrawerArrowDrawable,
        bottomSheetListener: () -> Boolean,
        onNavigationClick: () -> Unit
    ) {
        this.navigation = navigation
        this.drawerArrowDrawable = drawerArrowDrawable
        this.bottomSheetListener = bottomSheetListener

        binding.ivNavigation.setImageDrawable(drawerArrowDrawable)
        binding.ivNavigation.setOnClickListener { onNavigationClick() }
        onFragmentStackChanged(navigation.screensCount, true)
        navigation.addStackChangeListener(stackChangeListener)
    }

    fun setSearchModeEnabled(enabled: Boolean) {
        setSearchModeEnabled(enabled, true, false)
    }

    fun setSearchModeEnabled(
        enabled: Boolean,
        showKeyboard: Boolean,
        jumpToState: Boolean,
    ) {
        if (!::bottomSheetListener.isInitialized) {
            return  //uninitialized state
        }
        isInSearchMode = enabled
        searchModeSubject.onNext(enabled)
        binding.etSearch.visibility = if (enabled) VISIBLE else GONE
        binding.clTitleContainer.alpha = if (!enabled && isContentVisible) 1f else 0f
        binding.acvMain.visibility = if (enabled) GONE else VISIBLE
        if (!isDrawerArrowLocked()) {
            setCommandButtonMode(!enabled, !jumpToState)
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

    fun setupOptionsMenu(@MenuRes menuResId: Int, listener: ((MenuItem) -> Unit)) {
        ActionMenuUtil.setupMenu(binding.acvMain, menuResId, listener)
    }

    fun clearOptionsMenu() {
        ActionMenuUtil.setupMenu(binding.acvMain, R.menu.empty_stub_menu) { }
    }

    fun release() {
        navigation.removeStackChangeListener(stackChangeListener)
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

            // disabled because folder screens action mode issues
//            boolean inSelectionMode = bundle.getBoolean(IN_SELECTION_MODE);
//            setSelectionModeEnabled(inSelectionMode, false);
            state = bundle.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
    }

    fun setup(configCallback: (SetupConfig) -> Unit): AdvancedToolbar {
        val config = SetupConfig(context, getTitle(), getSubtitle())
        configCallback(config)
        setTitle(config.title)
        setSubtitle(config.subtitle)
        setupSearch(config.textChangeListener, config.searchText)
        if (config.menuListener == null) {
            clearOptionsMenu()
        } else {
            setupOptionsMenu(config.menuResId, config.menuListener!!)
        }
        setTitleClickListener(config.titleClickListener)
        if (config.selectionMenuListener != null) {
            setupSelectionModeMenu(config.selectionMenuResId, config.selectionMenuListener)
        }
        return this
    }

    fun getTitle() = binding.tvTitle.text

    fun setTitle(@StringRes titleId: Int) {
        setTitle(context.getString(titleId))
    }

    fun setTitle(title: CharSequence?) {
        binding.tvTitle.visibility = if (TextUtils.isEmpty(title)) GONE else VISIBLE
        binding.tvTitle.text = title
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
        binding.ivActionIcon.visibility = if (listener == null) GONE else VISIBLE
        binding.flTitleArea.isEnabled = listener != null
        binding.flTitleArea.setOnClickListener(listener)
    }

    fun clearTitleMenu() {
        setTitleClickListener(null)
    }

    fun setupTitleMenu(@MenuRes menuResId: Int, listener: ((MenuItem) -> Unit)) {
        setTitleClickListener { v -> showPopup(v, menuResId, Gravity.BOTTOM, listener) }
    }

    fun onStackFragmentSlided(offset: Float) {
        if (navigation.screensCount <= 2) {
            drawerArrowDrawable.progress = offset
        }
    }

    fun isInSearchMode() = isInSearchMode

    fun isInActionMode() = isInActionMode

    fun getActionMenuView(): ActionMenuView? = binding.acvMain

    fun setupSearch(textChangeListener: ((String) -> Unit)?, text: String?) {
        setupSearch(textChangeListener)
        binding.etSearch.setText(text)
        setSearchModeEnabled(!TextUtils.isEmpty(text))
    }

    fun setupSearch(textChangeListener: ((String) -> Unit)?) {
        this.textChangeListener = textChangeListener
        textConfirmListener = textChangeListener
    }

    fun isSearchLocked() =  !binding.etSearch.isEnabled

    fun setSearchLocked(locked: Boolean) {
        binding.etSearch.isEnabled = !locked
    }

    fun getSearchText() = binding.etSearch.text.toString()

    fun getSearchModeObservable(): Observable<Boolean> = searchModeSubject

    fun getSelectionModeObservable(): Observable<Boolean> = selectionModeSubject

    private fun onFragmentStackChanged(stackSize: Int, jumpToState: Boolean) {
        if (isInSearchMode && !jumpToState) {
            //close search on navigation back or forward. Ignore first event
            //possible improving: animate visibility with back button progress
            setSearchModeEnabled(false)
        }
        val isRoot = stackSize <= 1
        //hmm, not sure about search mode, check how it works
        if (isRoot && (bottomSheetListener() || isInSearchMode)) {
            return
        }
        setCommandButtonMode(isRoot, !jumpToState)
    }

    private fun setCommandButtonMode(isBase: Boolean, animate: Boolean) {
        val end = if (isBase) 0f else 1f
        if (animate) {
            val objectAnimator = getControlButtonAnimator(isBase)
            objectAnimator.duration = Constants.Animation.TOOLBAR_ARROW_ANIMATION_TIME
            objectAnimator.start()
        } else {
            drawerArrowDrawable.progress = end
        }
    }

    private fun getControlButtonAnimator(isArrow: Boolean): ValueAnimator {
        val start = drawerArrowDrawable.progress
        val end = if (isArrow) 0f else 1f
        val objectAnimator = ValueAnimator.ofFloat(start, end)
        objectAnimator.addUpdateListener { animation ->
            drawerArrowDrawable.progress = animation.animatedValue as Float
        }
        return objectAnimator
    }

    private fun onSearchTextViewAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (textConfirmListener != null) {
            textConfirmListener!!.invoke(v.text.toString())
            return true
        }
        return true
    }

    private fun onSearchTextChanged(text: String) {
        textChangeListener?.invoke(text)
    }

    fun setControlButtonProgress(slideOffset: Float) {
        if (!(navigation.screensCount > 1 || isInSearchMode || isInActionMode)) {
            drawerArrowDrawable.progress = slideOffset
        }
    }

    fun setControlButtonColor(@ColorInt color: Int) {
        drawerArrowDrawable.color = color
    }

    fun setupSelectionModeMenu(@MenuRes menuResource: Int, listener: ((MenuItem) -> Unit)?) {
        ActionMenuUtil.setupMenu(binding.acvSelection, menuResource, listener, 1)
    }

    fun editActionMenu(callback: (Menu) -> Unit) {
        callback(binding.acvSelection.menu)
    }

    fun showSelectionMode(count: Int) {
        if (count == 0 && isInActionMode) {
            setSelectionModeEnabled(false, true)
        }
        if (count > 0) {
            if (!isInActionMode) {
                setSelectionModeEnabled(true, true)
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
        binding.clTitleContainer.alpha = alpha
        isContentVisible = alpha == 1f
    }

    fun setContentVisible(visible: Boolean) {
        isContentVisible = visible
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
        var isHamburger = !enabled
        if (!enabled && isInSearchMode || navigation.screensCount > 1) {
            isHamburger = false
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
            mainAnimatorSet.play(getControlButtonAnimator(isHamburger))
                .with(
                    ViewUtils.getColorAnimator(
                        startControlButtonColor,
                        endControlButtonColor
                    ) { color -> drawerArrowDrawable.color = color }
                )
                .with(
                    ViewUtils.getBackgroundAnimator(
                        this,
                        startBackgroundColor,
                        endBackgroundColor
                    )
                )
                .with(
                    ViewUtils.getColorAnimator(
                        startStatusBarColor,
                        endStatusBarColor
                    ) { color -> AndroidUtils.setStatusBarColor(window, color) })
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
            setCommandButtonMode(isHamburger, false)
            if (isInSearchMode) {
                binding.etSearch.visibility = anotherElementsVisibility
            } else {
                binding.clTitleContainer.visibility = anotherElementsVisibility
                binding.acvMain.visibility = anotherElementsVisibility
            }
            binding.clSelectionMode.visibility = modeElementsVisibility
            drawerArrowDrawable.color = endControlButtonColor
            setBackgroundColor(endBackgroundColor)
            AndroidUtils.setStatusBarColor(window, endStatusBarColor)
        }
    }

    private fun isDrawerArrowLocked(): Boolean {
        return bottomSheetListener() || navigation.screensCount > 1
    }

    class SetupConfig(
        private val context: Context,
        var title: CharSequence,
        var subtitle: CharSequence?,
    ) {

        var textChangeListener: ((String) -> Unit)? = null
        private var textConfirmListener: ((String) -> Unit)? = null
        var searchText: String? = null

        @MenuRes
        var menuResId = 0
        var menuListener: ((MenuItem) -> Unit)? = null
        var titleClickListener: OnClickListener? = null

        @MenuRes
        var selectionMenuResId = 0
        var selectionMenuListener: ((MenuItem) -> Unit)? = null

        fun setupSearch(textChangeListener: ((String) -> Unit)?, text: String?) {
            this.textChangeListener = textChangeListener
            textConfirmListener = textChangeListener
            searchText = text
        }

        fun setupOptionsMenu(@MenuRes menuResId: Int, listener: ((MenuItem) -> Unit)?) {
            this.menuResId = menuResId
            menuListener = listener
        }

        fun setupSelectionModeMenu(@MenuRes menuResId: Int, listener: ((MenuItem) -> Unit)?) {
            selectionMenuResId = menuResId
            selectionMenuListener = listener
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
    }

    private inner class StackChangeListenerImpl : FragmentStackListener {
        override fun onStackChanged(stackSize: Int) {
            onFragmentStackChanged(navigation.screensCount, false)
        }
    }

    companion object {
        private const val IN_SEARCH_MODE = "in_search_mode"
        private const val IN_SELECTION_MODE = "in_selection_mode"
        private const val IS_KEYBOARD_SHOWN = "is_keyboard_shown"
    }

}