package de.kopis.timeclicker.utils;

import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.model.TimeSumWithDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

public class TimeSumUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(TimeSumUtility.class);

  public static double convertToHours(long workingDuration) {
    return BigDecimal.valueOf(workingDuration / (60.0 * 60.0 * 1000.0)).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * Calculates the {@link TimeSum} by summing up all {@link TimeEntry}s with the same date.
   *
   * @param allEntries list of {@link TimeEntry}s
   * @return a list of {@link TimeSumWithDate}s
   */
  public static List<TimeSumWithDate> calculateDailyTimeSum(final List<TimeEntry> allEntries) {
    // calculate overall sum per day
    final Map<Long, TimeSumWithDate> perDay = new HashMap<>();
    for (TimeEntry e : allEntries) {
      // build the key from given TimeEntry
      final Instant entryDate = e.getStart();
      final Calendar cal = Calendar.getInstance();
      cal.setTime(Date.from(entryDate));
      // reset to midnight
      makeMidnight(cal);
      // check if date is already set as key previously
      final TimeSumWithDate sum = putSum(perDay, cal);
      sum.addDuration(new TimeSum(e).getDuration());
    }

    final List<TimeSumWithDate> sortedPerDay = perDay.values().stream()
            .sorted(Comparator.comparing(TimeSumWithDate::getDate).reversed())
            .collect(toList());
    return sortedPerDay;
  }

  public List<TimeSumWithDate> calculateWeeklyTimeSum(final List<TimeEntry> allEntries) {
    final Map<Long, TimeSumWithDate> perWeek = new HashMap<>();

    for (TimeEntry e : allEntries) {
      // build the key from given TimeEntry
      final Instant entryDate = e.getStart();
      final Calendar cal = Calendar.getInstance();
      cal.setTime(Date.from(entryDate));
      // reset to first of week
      cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      // reset to midnight
      makeMidnight(cal);
      final TimeSumWithDate sum = putSum(perWeek, cal);
      final TimeSum timeSum = new TimeSum(e);
      sum.addDuration(timeSum.getDuration());
    }

    final List<TimeSumWithDate> sortedPerWeek = perWeek.values().stream()
            .sorted(Comparator.comparing(TimeSumWithDate::getDate).reversed())
            .collect(toList());
    return sortedPerWeek;
  }

  private static TimeSumWithDate putSum(Map<Long, TimeSumWithDate> sums, Calendar cal) {
    // check if date is already set as key previously
    final Long key = cal.getTimeInMillis();
    if (!sums.containsKey(key)) {
      sums.put(key, new TimeSumWithDate(cal.getTime(), Duration.of(0, ChronoUnit.SECONDS)));
    }
    // add sum to existing entry
    return sums.get(key);
  }

  public static List<TimeSumWithDate> calculateMonthlyTimeSum(final List<TimeEntry> allEntries) {
    final Map<Long, TimeSumWithDate> perMonth = new HashMap<>();

    for (TimeEntry e : allEntries) {
      // build the key from given TimeEntry
      final Instant entryDate = e.getStart();
      final Calendar cal = Calendar.getInstance();
      cal.setTime(Date.from(entryDate));
      makeFirstOfMonth(cal);
      // check if date is already set as key previously
      final Long key = cal.getTimeInMillis();
      if (!perMonth.containsKey(key)) {
        LOGGER.debug("Adding new key {} for instant {}", key, entryDate);
        perMonth.put(key, new TimeSumWithDate(cal.getTime(), Duration.of(0, ChronoUnit.SECONDS)));
      }
      // add sum to existing entry
      final TimeSumWithDate sum = perMonth.get(key);
      sum.addDuration(new TimeSum(e).getDuration());
    }

    final List<TimeSumWithDate> sortedPerMonth = perMonth.values().stream()
            .sorted(Comparator.comparing(TimeSumWithDate::getDate))
            .collect(toList());
    return sortedPerMonth;
  }

  public static Instant makeFirstOfMonth(Instant instant) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(Date.from(instant));
    makeFirstOfMonth(cal);
    return cal.getTime().toInstant();
  }

  public static Calendar makeFirstOfMonth(Calendar cal) {
    // reset to first of month
    cal.set(Calendar.DAY_OF_MONTH, 1);
    makeMidnight(cal);
    return cal;
  }

  public static Calendar makeLastOfMonth(Calendar month) {
    Instant last = makeLastOfMonth(month.toInstant());
    Calendar cal = Calendar.getInstance();
    cal.setTime(Date.from(last));
    return cal;
  }

  public static Instant makeLastOfMonth(Instant month) {
    final Calendar thisMonth = Calendar.getInstance();
    thisMonth.setTime(Date.from(month));
    final Calendar cal = Calendar.getInstance();
    cal.setTime(Date.from(month));
    while (cal.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH)) {
      cal.add(Calendar.DAY_OF_MONTH, 1);
    }
    // roll back one day, we counted 1 too far
    cal.add(Calendar.DAY_OF_MONTH, -1);
    LOGGER.debug("Last day of firstOfMonth: {}", cal.get(Calendar.DAY_OF_MONTH));
    return cal.getTime().toInstant();
  }

  private static void makeMidnight(Calendar cal) {
    // reset to midnight
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
  }
}