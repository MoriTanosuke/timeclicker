package de.kopis.timeclicker.model;

import de.kopis.timeclicker.Application;
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
  private final long expectedDuration;

  public MonthlyTimeSum(Instant month, Duration duration) {
    super(duration);
    this.firstOfMonth = makeFirstOfMonth(month);
    lastOfMonth = makeLastOfMonth(month);

    expectedDuration = WorkdayCalculator.getWorkingDays(this.firstOfMonth, lastOfMonth) * Application.HOURS_PER_DAY_IN_MILLISECONDS;
    LOGGER.debug("Setting expected duration for " + this.firstOfMonth + " to " + expectedDuration);
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
    LOGGER.debug("Last day of firstOfMonth: " + cal.get(Calendar.DAY_OF_MONTH));
    return cal.getTime().toInstant();
  }

  public static Instant makeFirstOfMonth(Instant d) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime(Date.from(d));
    // reset to first of firstOfMonth
    cal.set(Calendar.DAY_OF_MONTH, 1);
    // reset to midnight
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    return cal.getTime().toInstant();
  }

  public Instant getDate() {
    return firstOfMonth;
  }

  public long getExpectedDuration() {
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
