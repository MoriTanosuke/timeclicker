package de.kopis.timeclicker.utils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DurationUtilsTest {
  @Test
  public void testDuration() {
    assertEquals("25:01:01", DurationUtils.getReadableDuration(Duration.of(25 * 60 * 60 * 1000 + 61000, ChronoUnit.MILLIS)));
  }

  @Test
  public void testNegativeDuration() {
    assertEquals("-25:01:01", DurationUtils.getReadableDuration(Duration.of(-1 * (25 * 60 * 60 * 1000 + 61000), ChronoUnit.MILLIS)));
  }
}