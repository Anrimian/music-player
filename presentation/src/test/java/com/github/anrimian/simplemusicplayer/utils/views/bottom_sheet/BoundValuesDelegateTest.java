package com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created on 21.01.2018.
 */
public class BoundValuesDelegateTest {

    private BottomSheetDelegate mockDelegate = mock(BottomSheetDelegate.class);

    private BoundValuesDelegate boundValuesDelegate;

    @Before
    public void setUp() throws Exception {
        boundValuesDelegate = new BoundValuesDelegate(0.3f, 0.5f, mockDelegate);
    }

    @Test
    public void onSlideStartValuesTest() throws Exception {
        boundValuesDelegate.onSlide(0.0f);
        verify(mockDelegate).onSlide(eq(0.0f));

        boundValuesDelegate.onSlide(1.0f);
        verify(mockDelegate).onSlide(eq(1.0f));
    }

    @Test
    public void onSlideBorderValuesTest() throws Exception {
        boundValuesDelegate.onSlide(0.2f);
        verify(mockDelegate).onSlide(eq(0.0f));

        boundValuesDelegate.onSlide(0.6f);
        verify(mockDelegate).onSlide(eq(1.0f));
    }

    @Test
    public void onSlideInRangeValuesTest() throws Exception {
        boundValuesDelegate.onSlide(0.3f);
        verify(mockDelegate).onSlide(eq(0.0f));

        boundValuesDelegate.onSlide(0.31f);
        verify(mockDelegate).onSlide(eq(0.049999956f));

        boundValuesDelegate.onSlide(0.4f);
        verify(mockDelegate).onSlide(eq(0.5f));

        boundValuesDelegate.onSlide(0.5f);
        verify(mockDelegate).onSlide(eq(1.0f));
    }

}