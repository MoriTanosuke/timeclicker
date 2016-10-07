package de.kopis.timeclicker.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class WorkdayCalculatorTest {

    @Test
    public void testGetWorkingDaysBetweenTwoDates() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 14);
        // 2015-12-14
        final Date date1 = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 23);
        // 2015-12-23
        final Date date2 = cal.getTime();

        final int workdays = WorkdayCalculator.getWorkingDays(date1, date2);
        assertEquals(8, workdays);
    }

    @Test
    public void testGetWorkingDaysOnFirstDayOfMonth() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // 2016-02-01
        final Date date1 = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // 2016-02-01
        final Date date2 = cal.getTime();

        final int workdays = WorkdayCalculator.getWorkingDays(date1, date2);
        assertEquals(1, workdays);
    }

    @Test
    public void testGetWorkingDaysForSept2015() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // 2015-09-01
        final Date date1 = cal.getTime();
        // 2015-09-30
        cal.set(Calendar.DAY_OF_MONTH, 30);
        final Date date2 = cal.getTime();

        final int workdays = WorkdayCalculator.getWorkingDays(date1, date2);
        assertEquals(22, workdays);
    }

    @Test
    public void testGetWorkingDaysForWeek() {
        assertEquals(5, WorkdayCalculator.getWorkingDaysForCurrentWeek());
    }
}