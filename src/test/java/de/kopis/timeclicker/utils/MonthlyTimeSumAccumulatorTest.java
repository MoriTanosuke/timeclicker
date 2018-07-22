package de.kopis.timeclicker.utils;

import de.kopis.timeclicker.model.AbstractTimeEntryTest;
import de.kopis.timeclicker.model.TimeEntry;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MonthlyTimeSumAccumulatorTest extends AbstractTimeEntryTest {

    @Test
    public void canAccumulateTimeEntries() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 13);
        final Date d11 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        final Date d12 = cal.getTime();
        final TimeEntry entry1 = buildTimeEntry(d11, d12);

        cal.add(Calendar.MONTH, 1);
        final Date d21 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 3);
        final Date d22 = cal.getTime();
        final TimeEntry entry2 = buildTimeEntry(d21, d22);

        final MonthlyTimeSumAccumulator accumulator = new MonthlyTimeSumAccumulator();
        accumulator.accumulate(entry1);
        accumulator.accumulate(entry2);

        assertEquals(2, accumulator.getAll().size());
        assertEquals(Duration.of(2 * 60 * 60, ChronoUnit.SECONDS), accumulator.get(entry1.getStart()).getDuration());
        assertEquals(Duration.of(3 * 60 * 60, ChronoUnit.SECONDS), accumulator.get(entry2.getStart()).getDuration());
    }
}
