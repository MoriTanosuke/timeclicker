package de.kopis.timeclicker.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;

import de.kopis.timeclicker.WicketApplication;
import org.junit.Test;

public class MonthlyTimeSumTest extends AbstractTimeEntryTest {

    @Test
    public void testIsFirstOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 13);

        final MonthlyTimeSum sum = new MonthlyTimeSum(cal.getTime(), 0L);
        Calendar sumCal = Calendar.getInstance();
        sumCal.setTime(sum.getDate());
        assertEquals(Calendar.SEPTEMBER, sumCal.get(Calendar.MONTH));
        assertEquals(1, sumCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testCanCalculateExpectedDuration() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 13);
        // expect 30 days for Sept 2015, 22 workdays

        final MonthlyTimeSum sum = new MonthlyTimeSum(cal.getTime(), 1234L);
        Calendar sumCal = Calendar.getInstance();
        sumCal.setTime(sum.getDate());
        assertEquals(22 * WicketApplication.HOURS_PER_DAY_IN_MILLISECONDS, sum.getExpectedDuration());
    }

    @Test
    public void testCanAddTimeSumToMonthlyTimeSum() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 13);
        final MonthlyTimeSum sum = new MonthlyTimeSum(cal.getTime(), 0L);

        sum.add(new TimeSum(1234L));
        sum.add(new TimeSum(4321L));

        assertEquals(1234L + 4321L, sum.getDuration());
        assertEquals(22 * WicketApplication.HOURS_PER_DAY_IN_MILLISECONDS, sum.getExpectedDuration());
    }

    @Test
    public void canCollectTimeEntriesAndSumThem() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 13);

        final Date d1 = cal.getTime();
        // add one hour
        cal.add(Calendar.HOUR_OF_DAY, 1);
        final Date d2 = cal.getTime();
        final TimeEntry entryOneHour = buildTimeEntry(d1, d2);

        // add one hour
        cal.add(Calendar.HOUR_OF_DAY, 1);
        final Date d21 = cal.getTime();
        // add two hours
        cal.add(Calendar.HOUR_OF_DAY, 2);
        final Date d22 = cal.getTime();
        final TimeEntry entryTwoHours = buildTimeEntry(d21, d22);

        final MonthlyTimeSum sum1 = new MonthlyTimeSum(d1, 0L);
        sum1.add(entryOneHour);
        sum1.add(entryTwoHours);

        assertEquals(3 * 60 * 60 * 1000, sum1.getDuration());
        assertEquals("03:00:00", sum1.getReadableDuration());
    }

    @Test
    public void onlyAcceptsTimeEntriesForTheSameMonth() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 13);

        final Date d1 = cal.getTime();
        // add one hour
        cal.add(Calendar.HOUR_OF_DAY, 1);
        final Date d2 = cal.getTime();
        final TimeEntry entryInSeptember = buildTimeEntry(d1, d2);

        // add one month
        cal.add(Calendar.MONTH, 1);
        final Date d21 = cal.getTime();
        // add one hour
        cal.add(Calendar.HOUR_OF_DAY, 1);
        final Date d22 = cal.getTime();
        final TimeEntry entryInOctober = buildTimeEntry(d21, d22);

        final MonthlyTimeSum sum1 = new MonthlyTimeSum(d1, 0L);
        sum1.add(entryInSeptember);
        // this will throw an exception
        try {
            sum1.add(entryInOctober);
            fail("No exception thrown when adding entries from different months");
        } catch (Exception e) {
            // this is fine
        }
    }

}