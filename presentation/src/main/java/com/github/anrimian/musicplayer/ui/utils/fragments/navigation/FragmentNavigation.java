package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.content.res.Resources;
import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

public class FragmentNavigation {

    private static final String NAVIGATION_FRAGMENT_TAG = "navigation_fragment_tag";

    private final FragmentManagerProvider fragmentManagerProvider;
    private final LinkedList<FragmentCreator> fragments = new LinkedList<>();
    private final List<FragmentStackListener> stackListeners = new LinkedList<>();

    private final JugglerViewPresenter jugglerViewPresenter = new JugglerViewPresenter();
    private JugglerView jugglerView;

    private boolean isNavigationEnabled = true;

    private boolean checkOnEqualityOnReplace = false;

    @AnimRes private int enterAnimation = 0;
    @AnimRes private int exitAnimation = 0;
    @AnimRes private int rootEnterAnimation = 0;
    @AnimRes private int rootExitAnimation = 0;

    public static FragmentNavigation from(FragmentManager fm) {
        NavigationFragment container = (NavigationFragment) fm.findFragmentByTag(NAVIGATION_FRAGMENT_TAG);
        if (container == null) {
            container = new NavigationFragment();
            fm.beginTransaction()
                    .add(container, NAVIGATION_FRAGMENT_TAG)
                    .commit();
        }
        return container.getFragmentNavigation();
    }

    FragmentNavigation(FragmentManagerProvider fragmentManagerProvider) {
        this.fragmentManagerProvider = fragmentManagerProvider;
    }

    public void initialize(@NonNull JugglerView jugglerView) {
        this.jugglerView = Objects.requireNonNull(jugglerView);
        jugglerView.setPresenter(jugglerViewPresenter);
        jugglerViewPresenter.initializeView(jugglerView);

        hideBottomFragmentMenu();
        notifyFragmentMovedToTop(getFragmentOnTop());
    }

    public void addNewFragment(FragmentCreator fragmentCreator) {
        addNewFragment(fragmentCreator, enterAnimation);
    }

    //TODO create with exist stack feature

    public void addNewFragment(FragmentCreator fragmentCreator,
                               @AnimRes int enterAnimation) {
        checkForInitialization();
        if (!isNavigationEnabled) {
            return;
        }
        isNavigationEnabled = false;
        fragments.add(fragmentCreator);
        int id = jugglerView.prepareTopView();
        Fragment topFragment = fragmentCreator.createFragment();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enterAnimation, 0)
                .replace(id, topFragment)
                .runOnCommit(() -> {
                    isNavigationEnabled = true;
                    hideBottomFragmentMenu();
                    notifyStackListeners();
                    notifyFragmentMovedToTop(getFragmentOnTop());
                })
                .commit();
    }

    public void newRootFragment(FragmentCreator fragmentCreator) {
        newRootFragment(fragmentCreator, checkOnEqualityOnReplace, rootExitAnimation);
    }

    public void newRootFragment(FragmentCreator fragmentCreator, boolean checkForEquality) {
        newRootFragment(fragmentCreator, checkForEquality, rootExitAnimation);
    }

    public void newRootFragment(FragmentCreator fragmentCreator,
                                boolean checkForEquality,
                                @AnimRes int exitAnimation) {
        newRootFragment(fragmentCreator, checkForEquality, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragment(FragmentCreator fragmentCreator, @AnimRes int exitAnimation) {
        newRootFragment(fragmentCreator, checkOnEqualityOnReplace, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragment(FragmentCreator fragmentCreator,
                                @AnimRes int exitAnimation,
                                @AnimRes int enterAnimation) {
        newRootFragment(fragmentCreator, checkOnEqualityOnReplace, exitAnimation, enterAnimation);
    }

    public void newRootFragment(FragmentCreator fragmentCreator,
                                boolean checkForEquality,
                                @AnimRes int exitAnimation,
                                @AnimRes int enterAnimation) {
        checkForInitialization();
        if (!isNavigationEnabled) {
            return;
        }
        Fragment newRootFragment = fragmentCreator.createFragment();
        Fragment oldRootFragment = getFragmentOnTop();
        if (checkForEquality && equalClass(oldRootFragment, newRootFragment)) {
            return;
        }

        isNavigationEnabled = false;
        Fragment oldBottomFragment = getFragmentOnBottom();
        fragments.clear();
        fragments.add(fragmentCreator);
        int topViewId = jugglerViewPresenter.getTopViewId();
        FragmentTransaction transaction = fragmentManagerProvider.getFragmentManager()
                .beginTransaction();
        if (oldBottomFragment != null) {
            // I don't see it, but guess:
            // while oldTopFragment disappears, bottom fragment can be little visible.
            // How to check it?
            transaction.remove(oldBottomFragment);
        }
        transaction.setCustomAnimations(enterAnimation, exitAnimation)
                .replace(topViewId, newRootFragment)
                .runOnCommit(() -> {
                    isNavigationEnabled = true;
                    notifyStackListeners();
                    notifyFragmentMovedToTop(getFragmentOnTop());
                })
                .commit();
    }

    public boolean goBack() {
        return goBack(exitAnimation);
    }

    /**
     *
     * @return if back accepted or not
     */
    public boolean goBack(@AnimRes int exitAnimation) {
        checkForInitialization();
        if (fragments.size() <= 1) {
            return false;
        }
        if (!isNavigationEnabled) {
            return true;
        }
        isNavigationEnabled = false;
        fragments.removeLast();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, exitAnimation)
                .remove(getFragmentOnTop())
                .runOnCommit(() -> {
                    replaceBottomFragment(exitAnimation);
                    notifyStackListeners();
                })
                .commit();
        return true;
    }

    public void clearRootFragment(@AnimRes int exitAnimation) {
        checkForInitialization();
        if (fragments.size() < 1) {
            return;
        }
        if (fragments.size() > 1) {
            throw new IllegalStateException("can not clear: fragment is not root");
        }

        fragments.removeLast();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, exitAnimation)
                .remove(getFragmentOnTop())
                .runOnCommit(this::notifyStackListeners)
                .commit();
    }

    /**
     *
     * Don't forget to remove listener if you don't need it more
     *
     * @param listener to notify
     */
    public void addStackChangeListener(FragmentStackListener listener) {
        stackListeners.add(listener);
    }

    public void removeStackChangeListener(FragmentStackListener listener) {
        stackListeners.remove(listener);
    }

    public void clearStackChangeListeners() {
        stackListeners.clear();
    }

    public void checkForEqualityOnReplace(boolean checkOnEqualityOnReplace) {
        this.checkOnEqualityOnReplace = checkOnEqualityOnReplace;
    }

    public void setEnterAnimation(int enterAnimation) {
        this.enterAnimation = enterAnimation;
    }

    public void setExitAnimation(int exitAnimation) {
        this.exitAnimation = exitAnimation;
    }

    public void setRootEnterAnimation(int rootEnterAnimation) {
        this.rootEnterAnimation = rootEnterAnimation;
    }

    public void setRootExitAnimation(int rootExitAnimation) {
        this.rootExitAnimation = rootExitAnimation;
    }

    public int getScreensCount() {
        return fragments.size();
    }

    @Nullable
    public Fragment getFragmentOnTop() {
        return fragmentManagerProvider.getFragmentManager()
                .findFragmentById(jugglerViewPresenter.getTopViewId());
    }

    @Nullable
    public Fragment getFragmentOnBottom() {
        return fragmentManagerProvider.getFragmentManager()
                .findFragmentById(jugglerViewPresenter.getBottomViewId());
    }

    private void notifyFragmentMovedToTop(Fragment fragment) {
        if (fragment instanceof FragmentLayerListener) {
            ((FragmentLayerListener) fragment).onFragmentMovedOnTop();
        }
    }

    private void notifyStackListeners() {
        for (FragmentStackListener listener: stackListeners) {
            listener.onStackChanged(getScreensCount());
        }
    }

    private void replaceBottomFragment(@AnimRes int exitAnimation) {
        Fragment fragment = requireFragmentAtBottom();
        fragment.setMenuVisibility(true);
        notifyFragmentMovedToTop(fragment);

        jugglerView.postDelayed(() -> {
            int id = jugglerView.prepareBottomView();
            if (fragments.size() > 1) {
                Fragment bottomFragment = fragments.get(fragments.size() - 2).createFragment();//find better solution later
                bottomFragment.setMenuVisibility(false);
                fragmentManagerProvider.getFragmentManager()
                        .beginTransaction()
                        .replace(id, bottomFragment)
                        .runOnCommit(() -> isNavigationEnabled = true)
                        .commit();
            } else {
                isNavigationEnabled = true;
            }
        }, getAnimationDuration(exitAnimation));
    }

    private Fragment requireFragmentAtBottom() {
        Fragment fragment = getFragmentOnBottom();
        if (fragment == null) {
            throw new NullPointerException("required fragment from bottom is null");
        }
        return fragment;
    }

    private void hideBottomFragmentMenu() {
        Fragment fragment = getFragmentOnBottom();
        if (fragment != null) {
            fragment.setMenuVisibility(false);
        }
    }

    private long getAnimationDuration(@AnimRes int exitAnimation) {
        if (exitAnimation == 0) {
            return 0;
        }
        try {
            Animation animation = AnimationUtils.loadAnimation(jugglerView.getContext(), exitAnimation);
            return animation.getDuration();
        } catch (Resources.NotFoundException e) {
            return 0;
        }
    }

    private void checkForInitialization() {
        if (jugglerView == null) {
            throw new IllegalStateException("FragmentNavigator must be initialized first");
        }
    }

    private boolean equalClass(Object first, Object second) {
        return (first != null && first.getClass().equals(second.getClass()));
    }
}
