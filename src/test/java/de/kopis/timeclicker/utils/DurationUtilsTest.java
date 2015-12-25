package de.kopis.timeclicker.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DurationUtilsTest {
    @Test
    public void testDuration() {
        assertEquals("25 hours, 01 minutes, 01 seconds", DurationUtils.getReadableDuration(25 * 60 * 60 * 1000 + 61000));
    }
}