package de.kopis.timeclicker.model;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeEntryTest {
    final Duration duration = Duration.of(42, ChronoUnit.MINUTES);

    @Test
    public void hasNoDurationWhenStartAndStopNotPresent() {
        final TimeEntry t1 = new TimeEntry();
        t1.setStart(null);
        t1.setStop(null);
        assertEquals(Duration.ZERO, t1.getDuration());
    }

    @Test(expected = IllegalArgumentException.class)
    public void hasDurationWhenStopPresent() {
        final Instant now = Instant.now();
        final TimeEntry t1 = new TimeEntry();
        t1.setStart(null);
        t1.setStop(now);
        assertEquals(duration, t1.getDuration());
    }

    @Test
    public void hasDurationWhenStartPresent() {
        final Instant now = Instant.now();

        final TimeEntry t1 = new TimeEntry();
        t1.setStart(now.minus(duration));
        t1.setStop(null);

        // fake the stop supplier to have stable timings during testing
        t1.stopSupplier = () -> now;

        // calculated duration should now match the duration from now.minus(duration) -> now
        assertEquals(duration, t1.getDuration());
    }

    @Test
    public void hasDurationWhenStartAndStopPresent() {
        final Instant start = Instant.now();
        final TimeEntry t1 = new TimeEntry(start, start.plus(duration));
        assertEquals(duration, t1.getDuration());
    }

    @Test
    public void hasDurationWhenStartAndStopAndBreakPresent() {
        final Instant start = Instant.now();
        final TimeEntry t1 = new TimeEntry(start, start.plus(duration));
        final Duration breakDuration = Duration.of(5, ChronoUnit.MINUTES);
        t1.setBreakDuration(breakDuration);
        assertEquals(duration.minus(breakDuration), t1.getDuration());
    }

}