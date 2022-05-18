package com.github.anrimian.musicplayer.ui.common.format;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FormatUtilsTest {

    @Test
    public void formatDecibels() {
        assertEquals("+15.00 dB", FormatUtils.formatDecibels((short) 1500));
        assertEquals("+0.75 dB", FormatUtils.formatDecibels((short) 75));
        assertEquals("+1.75 dB", FormatUtils.formatDecibels((short) 175));
        assertEquals("-1.75 dB", FormatUtils.formatDecibels((short) -175));
        assertEquals("-15.00 dB", FormatUtils.formatDecibels((short) -1500));
        assertEquals("0.00 dB", FormatUtils.formatDecibels((short) 0));
    }

    @Test
    public void formatHz() {
    }
}