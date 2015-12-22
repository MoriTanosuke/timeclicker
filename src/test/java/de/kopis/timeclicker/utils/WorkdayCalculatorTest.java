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
}