package de.kopis.timeclicker.utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkdayCalculator {
  private static final Logger LOGGER = LoggerFactory.getLogger(WorkdayCalculator.class);
  private static final List<Integer> WORKDAYS_WEEK = Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY);

  public static int getWorkingDays(final Instant startDate, final Instant endDate) {
    LOGGER.debug("Calculating work days from " + startDate + " to " + endDate);

    final Calendar startCal = Calendar.getInstance();
    startCal.setTime(Date.from(startDate));
    startCal.set(Calendar.HOUR_OF_DAY, 0);
    startCal.set(Calendar.MINUTE, 0);
    startCal.set(Calendar.SECOND, 0);
    startCal.set(Calendar.MILLISECOND, 0);

    final Calendar endCal = Calendar.getInstance();
    endCal.setTime(Date.from(endDate));
    endCal.set(Calendar.HOUR_OF_DAY, 0);
    endCal.set(Calendar.MINUTE, 0);
    endCal.set(Calendar.SECOND, 0);
    endCal.set(Calendar.MILLISECOND, 0);

    int workDays = 0;

    //Return 1 if start and end are the same
    if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
      return 1;
    }

    while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()) {
      if (WORKDAYS_WEEK.contains(startCal.get(Calendar.DAY_OF_WEEK))) {
        ++workDays;
      }
      startCal.add(Calendar.DAY_OF_MONTH, 1);
    }

    // dirty workaround for div by zero
    if (workDays == 0) {
      workDays = 1;
    }

    LOGGER.debug("Workdays: " + workDays);
    return workDays;
  }

  public static int getWorkingDaysForCurrentMonth() {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    final Date startOfMonth = cal.getTime();
    cal.roll(Calendar.DAY_OF_MONTH, false);
    final Date endOfMonth = cal.getTime();
    return WorkdayCalculator.getWorkingDays(startOfMonth.toInstant(), endOfMonth.toInstant());
  }

  public static int getWorkingDaysForCurrentWeek() {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
    final Date start = cal.getTime();
    cal.add(Calendar.DAY_OF_WEEK, 7);
    final Date end = cal.getTime();
    return WorkdayCalculator.getWorkingDays(start.toInstant(), end.toInstant());
  }
}
