package de.kopis.timeclicker.model;

import de.kopis.timeclicker.utils.TimeSumUtility;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeSumUtilityTest {
  @Test
  public void convertToHours() {
    assertEquals(8, TimeSumUtility.convertToHours(8 * 60 * 60 * 1000), 0.00001);
  }

  @Test
  public void aggregateByDate() {
    final Calendar cal = Calendar.getInstance();

    // add 1 entry today, and 2 entries tomorrow
    final TimeEntry t1 = new TimeEntry(cal.getTime().toInstant());
    t1.setStop(t1.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    cal.add(Calendar.DAY_OF_YEAR, 1);
    final TimeEntry t2 = new TimeEntry(cal.getTime().toInstant());
    t2.setStop(t2.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    final TimeEntry t3 = new TimeEntry(cal.getTime().toInstant());
    t3.setStop(t3.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    final List<TimeEntry> entries = Arrays.asList(t1, t2, t3);

    List<TimeSumWithDate> values = TimeSumUtility.calculateDailyTimeSum(entries);
    assertEquals(2, values.size());
    assertEquals(Duration.of(84, ChronoUnit.SECONDS), values.get(0).getDuration());
    assertEquals(Duration.of(42, ChronoUnit.SECONDS), values.get(1).getDuration());
  }

  @Test
  public void aggregateByMonth() {
    final Calendar cal = Calendar.getInstance();
    final Instant start = cal.getTime().toInstant();

    // add 1 entry today, and 2 entries tomorrow
    final TimeEntry t1 = new TimeEntry(start);
    t1.setStop(t1.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    final TimeEntry t11 = new TimeEntry(start);
    t11.setStop(t11.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    cal.add(Calendar.MONTH, -2);
    final TimeEntry t2 = new TimeEntry(start);
    t2.setStop(t2.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    cal.add(Calendar.MONTH, -1);
    final TimeEntry t3 = new TimeEntry(start);
    t3.setStop(t3.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    final List<TimeEntry> entries = Arrays.asList(t1, t11, t2, t3);

    List<TimeSumWithDate> values = TimeSumUtility.calculateMonthlyTimeSum(entries);
    assertEquals(1, values.size());
    assertEquals(Duration.of(84 + 42 + 42, ChronoUnit.SECONDS), values.get(0).getDuration());
  }

  @Test
  public void aggregateByWeek() {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
    // last week
    cal.add(Calendar.WEEK_OF_YEAR, -1);

    // add entries last week
    final TimeEntry t1 = new TimeEntry(cal.getTime().toInstant());
    t1.setStop(t1.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    cal.add(Calendar.DAY_OF_WEEK, 1);
    final TimeEntry t2 = new TimeEntry(cal.getTime().toInstant());
    t2.setStop(t2.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    // add one entry next week
    cal.add(Calendar.WEEK_OF_YEAR, 1);
    final TimeEntry t3 = new TimeEntry(cal.getTime().toInstant());
    t3.setStop(t3.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    final List<TimeEntry> entries = Arrays.asList(t1, t2, t3);

    List<TimeSumWithDate> values = new TimeSumUtility().calculateWeeklyTimeSum(entries);
    assertEquals(2, values.size());
    // sorted per week
    assertEquals(Duration.of(42, ChronoUnit.SECONDS), values.get(0).getDuration());
    assertEquals(Duration.of(84, ChronoUnit.SECONDS), values.get(1).getDuration());
  }

  @Test
  public void canGetFirstOfMonth() {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1984);
    cal.set(Calendar.MONTH, Calendar.FEBRUARY);
    cal.set(Calendar.DAY_OF_MONTH, 24);
    TimeSumUtility.makeFirstOfMonth(cal);
    assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  public void canGetLastOfMonth() {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1984);
    cal.set(Calendar.MONTH, Calendar.FEBRUARY);
    cal.set(Calendar.DAY_OF_MONTH, 24);
    final Calendar last = TimeSumUtility.makeLastOfMonth(cal);
    // that february had 29 days
    assertEquals(29, last.get(Calendar.DAY_OF_MONTH));
  }
}