package de.kopis.timeclicker.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.kopis.timeclicker.utils.TimeSumUtility;
import org.junit.Test;

public class TimeSumUtilityTest {
    @Test
    public void sortKeysByDate() throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Map<Date, Number> sums = new HashMap<>();
        final Date earlierDate = dateFormat.parse("1234-12-12 12:34:56 +0200");
        final Date laterDate = dateFormat.parse("2341-12-12 12:34:56 +0200");
        sums.put(laterDate, 42);
        sums.put(earlierDate, 21);
        final String[] keys = new TimeSumUtility().getSortedKeys(dateFormat, sums);
        final String[] expectedSortedKeys = {dateFormat.format(earlierDate), dateFormat.format(laterDate)};
        assertArrayEquals(expectedSortedKeys, keys);
    }

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