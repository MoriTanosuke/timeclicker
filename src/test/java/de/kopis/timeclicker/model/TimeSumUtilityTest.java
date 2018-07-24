package de.kopis.timeclicker.model;

import de.kopis.timeclicker.utils.TimeSumUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TimeSumUtilityTest {
  @Test
  public void convertToHours() {
    assertEquals(8, new TimeSumUtility().convertToHours(8 * 60 * 60 * 1000), 0.00001);
  }

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
    final TimeEntry t1 = new TimeEntry(cal.getTime().toInstant());
    t1.setStop(t1.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    cal.add(Calendar.DAY_OF_YEAR, 1);
    final TimeEntry t2 = new TimeEntry(cal.getTime().toInstant());
    t2.setStop(t2.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    final TimeEntry t3 = new TimeEntry(cal.getTime().toInstant());
    t3.setStop(t3.getStart().plus(Duration.of(42, ChronoUnit.SECONDS)));
    final List<TimeEntry> entries = Arrays.asList(t1, t2, t3);

    List<TimeSumWithDate> values = new TimeSumUtility().calculateDailyTimeSum(entries);
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

    List<TimeSumWithDate> values = new TimeSumUtility().calculateMonthlyTimeSum(entries);
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
}