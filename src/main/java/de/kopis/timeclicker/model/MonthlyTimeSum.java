package de.kopis.timeclicker.model;

import de.kopis.timeclicker.utils.TimeSumUtility;
import de.kopis.timeclicker.utils.WorkdayCalculator;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonthlyTimeSum extends TimeSum {
  private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyTimeSum.class);

  private final Instant firstOfMonth;
  private final Instant lastOfMonth;
  private final Duration expectedDuration;

  public MonthlyTimeSum(Instant month, Duration duration, Duration workPerDay) {
    super(duration);
    firstOfMonth = TimeSumUtility.makeFirstOfMonth(month);
    lastOfMonth = TimeSumUtility.makeLastOfMonth(month);

    final int workingDays = WorkdayCalculator.getWorkingDays(firstOfMonth, lastOfMonth);
    // TODO get user settings
    expectedDuration = workPerDay.multipliedBy(workingDays);
    LOGGER.debug("Setting expected duration for {} to {}", firstOfMonth, expectedDuration);
  }

  public Instant getDate() {
    return firstOfMonth;
  }

  public Duration getExpectedDuration() {
    return expectedDuration;
  }

  public void add(final TimeEntry entry) {
    validateInMonth(entry);
    add(new TimeSum(entry));
  }

  /**
   * Checks if the given {@link TimeEntry} is in the month of this sum.
   *
   * @param entry a {@link TimeEntry} to add to this sum
   * @throws IllegalArgumentException if entry is outside of month
   */
  private void validateInMonth(final TimeEntry entry) {
    final Calendar first = Calendar.getInstance();
    first.setTime(Date.from(firstOfMonth));
    final Calendar last = Calendar.getInstance();
    last.setTime(Date.from(lastOfMonth));
    final Calendar current = Calendar.getInstance();
    // check only on start date
    current.setTime(Date.from(entry.getStart()));
    if (current.before(first)) {
      throw new IllegalArgumentException(entry + " started before " + first);
    }
    if (current.after(last)) {
      throw new IllegalArgumentException(entry + " started after " + last);
    }
  }
}
