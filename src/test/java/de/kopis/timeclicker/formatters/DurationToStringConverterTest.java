package de.kopis.timeclicker.formatters;

import java.time.Duration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DurationToStringConverterTest {

  @Test
  public void convert() {
    // 3h 30m 30s
    final Duration duration = Duration.ofSeconds(3 * 60 * 60 + 30 * 60 + 30);
    String result = new DurationToStringConverter().convert(duration);
    assertEquals(duration.toString(), "210", result);
  }
}
