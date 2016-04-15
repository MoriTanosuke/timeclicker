package de.kopis.timeclicker.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import de.kopis.timeclicker.utils.TimeSumUtility;

public class TimeSumUtilityTest {
    @Test
    public void aggregateByDate() {
        final Calendar cal = Calendar.getInstance();

        // add 1 entry today, and 2 entries tomorrow
        final TimeEntry t1 = new TimeEntry(cal.getTime());
        t1.setStop(new Date(t1.getStart().getTime() + 42 * 1000));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        final TimeEntry t2 = new TimeEntry(cal.getTime());
        t2.setStop(new Date(t2.getStart().getTime() + 42 * 1000));
        final TimeEntry t3 = new TimeEntry(cal.getTime());
        t3.setStop(new Date(t3.getStart().getTime() + 42 * 1000));
        final List<TimeEntry> entries = Arrays.asList(t1, t2, t3);

        // TODO group by day
        List<TimeSumWithDate> values = new TimeSumUtility().calculateDailyTimeSum(entries);
        assertEquals(2, values.size());
        assertEquals(84000L, values.get(0).getDuration());
        assertEquals(42000L, values.get(1).getDuration());
    }
}