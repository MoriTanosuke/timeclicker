package de.kopis.timeclicker.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DurationUtilsTest {
    @Test
    public void testDuration() {
        assertEquals("25 hours, 01 minutes, 01 seconds", DurationUtils.getReadableDuration(25 * 60 * 60 * 1000 + 61000));
    }

    @Test
    public void testNegativeDuration() {
        assertEquals("-25 hours, 01 minutes, 01 seconds", DurationUtils.getReadableDuration(-1 * (25 * 60 * 60 * 1000 + 61000)));
    }
}