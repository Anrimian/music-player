package com.github.anrimian.musicplayer.utils.views.bottom_sheet;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created on 21.01.2018.
 */
public class BoundValuesDelegateTest {

    private final SlideDelegate mockDelegate = mock(SlideDelegate.class);

    private BoundValuesDelegate boundValuesDelegate;

    @BeforeEach
    public void setUp() {
        boundValuesDelegate = new BoundValuesDelegate(0.3f, 0.5f, mockDelegate);
    }

    @Test
    public void onSlideStartValuesTest() {
        boundValuesDelegate.onSlide(0.0f);
        verify(mockDelegate).onSlide(eq(0.0f));

        boundValuesDelegate.onSlide(1.0f);
        verify(mockDelegate).onSlide(eq(1.0f));
    }

    @Test
    public void onSlideBorderValuesTest() {
        boundValuesDelegate.onSlide(0.2f);
        verify(mockDelegate).onSlide(eq(0.0f));

        boundValuesDelegate.onSlide(0.6f);
        verify(mockDelegate).onSlide(eq(1.0f));
    }

    @Test
    public void onSlideInRangeValuesTest() {
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