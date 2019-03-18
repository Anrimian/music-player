package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class FragmentNavigation {

    private static final String NAVIGATION_FRAGMENT_TAG = "navigation_fragment_tag";
    private static final String SCREENS = "screens";

    private final FragmentManagerProvider fragmentManagerProvider;
    private final LinkedList<FragmentMetaData> screens = new LinkedList<>();
    private final List<FragmentStackListener> stackListeners = new LinkedList<>();

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

    public void initialize(@NonNull JugglerView jugglerView, @Nullable Bundle savedState) {
        this.jugglerView = jugglerView;
        jugglerView.initialize(savedState);

        if (!screens.isEmpty()) {//just orientation change
            hideBottomFragmentMenu();
            notifyFragmentMovedToTop(getFragmentOnTop());
            return;
        }
        if (savedState != null) {
            ArrayList<Bundle> bundleFragments = savedState.getParcelableArrayList(SCREENS);
            if (bundleFragments != null) {
                screens.addAll(mapList(bundleFragments, FragmentMetaData::new));
                notifyFragmentMovedToTop(getFragmentOnTop());
            }
        }
    }

    public void onSaveInstanceState(Bundle state) {
        jugglerView.saveInstanceState(state);
        state.putParcelableArrayList(SCREENS, getBundleScreens());
    }

    public void newRootFragmentStack(List<Fragment> fragments) {
        newRootFragmentStack(fragments, rootExitAnimation);
    }

    public void newRootFragmentStack(List<Fragment> fragments,
                                     @AnimRes int exitAnimation) {
        newRootFragmentStack(fragments, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragmentStack(List<Fragment> fragments,
                                     @AnimRes int exitAnimation,
                                     @AnimRes int enterAnimation) {
        checkForInitialization();
        if (!isNavigationEnabled) {
            return;
        }
        if (fragments.isEmpty()) {
            return;
        }
        if (fragments.size() == 1) {
            newRootFragment(fragments.get(0), exitAnimation, enterAnimation);
            return;
        }

        isNavigationEnabled = false;
        screens.clear();
        screens.addAll(mapList(fragments, FragmentMetaData::new));
        int id = jugglerView.getTopViewId();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation)
                .replace(id, fragments.get(fragments.size() - 1))
                .runOnCommit(() -> {
                    hideBottomFragmentMenu();
                    notifyStackListeners();
                    notifyFragmentMovedToTop(getFragmentOnTop());

                    jugglerView.postDelayed(
                            () -> {
                                if (fragments.size() > 1) {
                                    silentlyReplaceBottomFragment();
                                }
                                isNavigationEnabled = true;
                            },
                            getAnimationDuration(enterAnimation)
                    );

                })
                .commit();

    }

    public void addNewFragmentStack(List<Fragment> fragments) {
        addNewFragmentStack(fragments, enterAnimation);
    }

    public void addNewFragmentStack(List<Fragment> fragments, @AnimRes int enterAnimation) {
        checkForInitialization();
        if (!isNavigationEnabled) {
            return;
        }
        if (fragments.isEmpty()) {
            return;
        }
        if (fragments.size() == 1) {
            addNewFragment(fragments.get(0), enterAnimation);
            return;
        }
        isNavigationEnabled = false;
        screens.addAll(mapList(fragments, FragmentMetaData::new));
        int id = jugglerView.prepareTopView();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enterAnimation, 0)
                .replace(id, fragments.get(fragments.size() - 1))
                .runOnCommit(() -> {
                    hideBottomFragmentMenu();
                    notifyStackListeners();
                    notifyFragmentMovedToTop(getFragmentOnTop());

                    jugglerView.postDelayed(
                            () -> {
                                if (fragments.size() > 1) {
                                    silentlyReplaceBottomFragment();
                                }
                                isNavigationEnabled = true;
                            },
                            getAnimationDuration(enterAnimation)
                    );

                })
                .commit();

    }

    public void addNewFragment(Fragment fragment) {
        addNewFragment(fragment, enterAnimation);
    }

    public void addNewFragment(Fragment fragment,
                               @AnimRes int enterAnimation) {
        checkForInitialization();
        if (!isNavigationEnabled) {
            return;
        }
        isNavigationEnabled = false;
        screens.add(new FragmentMetaData(fragment));
        int id = jugglerView.prepareTopView();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enterAnimation, 0)
                .replace(id, fragment)
                .runOnCommit(() -> {
                    hideBottomFragmentMenu();
                    notifyStackListeners();
                    notifyFragmentMovedToTop(getFragmentOnTop());
                    jugglerView.postDelayed(
                            () -> isNavigationEnabled = true,
                            getAnimationDuration(enterAnimation)
                    );
                })
                .commit();
    }

    public void newRootFragment(Fragment fragment) {
        newRootFragment(fragment, checkOnEqualityOnReplace, rootExitAnimation);
    }

    public void newRootFragment(Fragment fragment, boolean checkForEquality) {
        newRootFragment(fragment, checkForEquality, rootExitAnimation);
    }

    public void newRootFragment(Fragment fragment,
                                boolean checkForEquality,
                                @AnimRes int exitAnimation) {
        newRootFragment(fragment, checkForEquality, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragment(Fragment fragment, @AnimRes int exitAnimation) {
        newRootFragment(fragment, checkOnEqualityOnReplace, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragment(Fragment fragment,
                                @AnimRes int exitAnimation,
                                @AnimRes int enterAnimation) {
        newRootFragment(fragment, checkOnEqualityOnReplace, exitAnimation, enterAnimation);
    }

    public void newRootFragment(Fragment newRootFragment,
                                boolean checkForEquality,
                                @AnimRes int exitAnimation,
                                @AnimRes int enterAnimation) {
        checkForInitialization();
        if (!isNavigationEnabled) {
            return;
        }
        Fragment oldRootFragment = getFragmentOnTop();
        if (checkForEquality && equalClass(oldRootFragment, newRootFragment)) {
            return;
        }

        isNavigationEnabled = false;
        screens.clear();
        screens.add(new FragmentMetaData(newRootFragment));
        int topViewId = jugglerView.getTopViewId();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation)
                .replace(topViewId, newRootFragment)
                .runOnCommit(() -> {
                    notifyStackListeners();
                    notifyFragmentMovedToTop(getFragmentOnTop());
                    jugglerView.postDelayed(
                            () -> {
                                silentlyClearBottomFragment();
                                isNavigationEnabled = true;
                            },
                            getAnimationDuration(enterAnimation)
                    );
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
        if (screens.size() <= 1) {
            return false;
        }
        if (!isNavigationEnabled) {
            return true;
        }
        Fragment fragmentOnTop = getFragmentOnTop();
        if (fragmentOnTop == null) {
            return false;
        }
        isNavigationEnabled = false;
        screens.removeLast();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, exitAnimation)
                .remove(fragmentOnTop)
                .runOnCommit(() -> {
                    moveBottomFragmentToTop(exitAnimation);
                    notifyStackListeners();
                })
                .commit();
        return true;
    }

    public void clearRootFragment(@AnimRes int exitAnimation) {
        checkForInitialization();
        if (screens.size() < 1) {
            return;
        }
        if (screens.size() > 1) {
            throw new IllegalStateException("can not clear: fragment is not root");
        }
        Fragment fragmentOnTop = getFragmentOnTop();
        if (fragmentOnTop == null) {
            return;
        }
        screens.removeLast();
        fragmentManagerProvider.getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, exitAnimation)
                .remove(fragmentOnTop)
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
        return screens.size();
    }

    public int getStackScreensCount() {
        int count = screens.size() - 1;
        return count < 0? 0: count;
    }

    public boolean hasScreens() {
        return !screens.isEmpty();
    }

    @Nullable
    public Fragment getFragmentOnTop() {
        return fragmentManagerProvider.getFragmentManager()
                .findFragmentById(jugglerView.getTopViewId());
    }

    @Nullable
    public Fragment getFragmentOnBottom() {
        return fragmentManagerProvider.getFragmentManager()
                .findFragmentById(jugglerView.getBottomViewId());
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

    private void silentlyReplaceBottomFragment() {
        if (screens.size() > 1) {
            FragmentMetaData bottomFragment = screens.get(screens.size() - 2);
            fragmentManagerProvider.getFragmentManager()
                    .beginTransaction()
                    .replace(jugglerView.getBottomViewId(), createFragment(bottomFragment))
                    .runOnCommit(this::hideBottomFragmentMenu)
                    .commit();//repeat java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        }
    }

    private void silentlyClearBottomFragment() {
        Fragment fragment = getFragmentOnBottom();
        if (fragment != null) {
            fragmentManagerProvider.getFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .runOnCommit(() -> fragment.setMenuVisibility(false))
                    .commit();
        }
    }

    private void moveBottomFragmentToTop(@AnimRes int exitAnimation) {
        Fragment fragment = requireFragmentAtBottom();
        fragment.setMenuVisibility(true);
        notifyFragmentMovedToTop(fragment);

        jugglerView.postDelayed(() -> {
            int id = jugglerView.prepareBottomView();
            if (screens.size() > 1) {
                FragmentMetaData metaData = screens.get(screens.size() - 2);
                Fragment bottomFragment = createFragment(metaData);
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

    private Fragment createFragment(FragmentMetaData metaData) {
        return fragmentManagerProvider.getFragmentManager()
                .getFragmentFactory()
                .instantiate(jugglerView.getContext().getClassLoader(),
                        metaData.getFragmentClassName(),
                        metaData.getArguments()
                );
    }

    private ArrayList<Bundle> getBundleScreens() {
        ArrayList<Bundle> screens = new ArrayList<>(this.screens.size());
        for (FragmentMetaData metaData: this.screens) {
            screens.add(metaData.toBundle());
        }
        return screens;
    }

    private boolean equalClass(@Nullable Object first, @NonNull Object second) {
        return (first != null && first.getClass().equals(second.getClass()));
    }
}
