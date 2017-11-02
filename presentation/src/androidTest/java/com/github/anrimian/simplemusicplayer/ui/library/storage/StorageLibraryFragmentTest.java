package com.github.anrimian.simplemusicplayer.ui.library.storage;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.mock;

/**
 * Created on 29.10.2017.
 */

@RunWith(AndroidJUnit4.class)
public class StorageLibraryFragmentTest {

    private StorageLibraryPresenter presenter;
    private StorageLibraryFragment fragment;

    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        presenter = mock(StorageLibraryPresenter.class);
        fragment = new StorageLibraryFragment() {
            @Override
            StorageLibraryPresenter providePresenter() {
                return presenter;
            }
        };

        activityActivityTestRule.getActivity()
                .getSupportFragmentManager()
                .beginTransaction();
    }

    @Test
    public void testTest() {
        //presenter.onFirstViewAttach();

        onView(withId(R.id.psv_progress_bar)).check(matches((isDisplayed())));
//        Assert.assertNotNull();
    }


}