package de.kopis.timeclicker.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.kopis.timeclicker.utils.TimeSumUtility;
import org.junit.Test;

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

        List<TimeSumWithDate> values = new TimeSumUtility().calculateDailyTimeSum(entries);
        assertEquals(2, values.size());
        assertEquals(84000L, values.get(0).getDuration());
        assertEquals(42000L, values.get(1).getDuration());
    }

    @Test
    public void aggregateByMonth() {
        final Calendar cal = Calendar.getInstance();

        // add 1 entry today, and 2 entries tomorrow
        final TimeEntry t1 = new TimeEntry(cal.getTime());
        t1.setStop(new Date(t1.getStart().getTime() + 42 * 1000));
        final TimeEntry t11 = new TimeEntry(cal.getTime());
        t11.setStop(new Date(t11.getStart().getTime() + 42 * 1000));
        cal.add(Calendar.MONTH, -2);
        final TimeEntry t2 = new TimeEntry(cal.getTime());
        t2.setStop(new Date(t2.getStart().getTime() + 42 * 1000));
        cal.add(Calendar.MONTH, -1);
        final TimeEntry t3 = new TimeEntry(cal.getTime());
        t3.setStop(new Date(t3.getStart().getTime() + 42 * 1000));
        final List<TimeEntry> entries = Arrays.asList(t1, t11, t2, t3);

        List<TimeSumWithDate> values = new TimeSumUtility().calculateMonthlyTimeSum(entries);
        assertEquals(3, values.size());
        assertEquals(84000L, values.get(0).getDuration());
        assertEquals(42000L, values.get(1).getDuration());
        assertEquals(42000L, values.get(2).getDuration());
    }

    @Test
    public void aggregateByWeek() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        // last week
        cal.add(Calendar.WEEK_OF_YEAR, -1);

        // add entries last week
        final TimeEntry t1 = new TimeEntry(cal.getTime());
        t1.setStop(new Date(t1.getStart().getTime() + 42 * 1000));
        cal.add(Calendar.DAY_OF_WEEK, 1);
        final TimeEntry t2 = new TimeEntry(cal.getTime());
        t2.setStop(new Date(t2.getStart().getTime() + 42 * 1000));
        // add one entry next week
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        final TimeEntry t3 = new TimeEntry(cal.getTime());
        t3.setStop(new Date(t3.getStart().getTime() + 42 * 1000));
        final List<TimeEntry> entries = Arrays.asList(t1, t2, t3);

        List<TimeSumWithDate> values = new TimeSumUtility().calculateWeeklyTimeSum(entries);
        assertEquals(2, values.size());
        // sorted per week
        assertEquals(42000L, values.get(0).getDuration());
        assertEquals(84000L, values.get(1).getDuration());
    }
}