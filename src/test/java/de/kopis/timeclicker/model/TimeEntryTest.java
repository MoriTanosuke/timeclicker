package de.kopis.timeclicker.model;

import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;

public class TimeEntryTest {
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
        final Map<Long, TimeSum> sumByDay = new HashMap<>();
        for (TimeEntry entry : entries) {
            final Calendar start = Calendar.getInstance();
            start.setTime(entry.getStart());
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            if (!sumByDay.containsKey(start.getTime().getTime())) {
                final TimeSum timeSum = new TimeSum(entry);
                sumByDay.put(start.getTime().getTime(), timeSum);
                System.out.println(">>> adding new sum " + start.getTime().getTime() + ":" + timeSum);
            } else {
                final TimeSum sum = sumByDay.get(start.getTime().getTime());
                System.out.println(">>> existing sum " + sum);
                sum.addDuration(entry);
                System.out.println(">>> added duration to timeSum" + sum);
            }
        }

        assertEquals(2, sumByDay.keySet().size());

        TimeSum[] values = sumByDay.values().toArray(new TimeSum[0]);
        assertEquals(42 * 1000, values[0].getDurationInMillis());
        assertEquals(2 * 42 * 1000, values[1].getDurationInMillis());
    }
}