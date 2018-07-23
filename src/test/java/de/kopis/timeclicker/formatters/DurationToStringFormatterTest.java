package de.kopis.timeclicker.formatters;

import java.text.ParseException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DurationToStringFormatterTest {
    private final Duration duration = Duration.of(42, ChronoUnit.MINUTES);

    @Test
    public void canFormat() {
        assertEquals("00:42:00",
                new DurationToStringFormatter().print(duration, Locale.getDefault()));
    }

    @Test
    public void canParse() throws ParseException {
        assertEquals(duration,
                new DurationToStringFormatter().parse("00:42:00", Locale.getDefault()));
    }

}