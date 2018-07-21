package de.kopis.timeclicker.utils;

import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import de.kopis.timeclicker.model.TimeSumWithDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.*;

public class TimeSumUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSumUtility.class);

    public static double convertToHours(long workingDuration) {
        return new BigDecimal(workingDuration / (60.0 * 60.0 * 1000.0)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public String[] getSortedKeys(final DateFormat dateFormat, final Map<Date, Number> mapOfSums) {
        String[] dates = new String[mapOfSums.size()];
        final Set<Date> keys = new TreeSet<>(mapOfSums.keySet());
        int i = 0;
        for(Date date : keys) {
            dates[i++] = dateFormat.format(date);
        }
        return dates;
    }

    /**
     * Calculates the {@link TimeSum} by summing up all {@link TimeEntry}s with the same date.
     *
     * @param allEntries list of {@link TimeEntry}s
     * @return a list of {@link TimeSumWithDate}s
     */
    public List<TimeSumWithDate> calculateDailyTimeSum(final List<TimeEntry> allEntries) {
        // calculate overall sum per day
        final Map<Long, TimeSumWithDate> perDay = new HashMap<>();
        for (TimeEntry e : allEntries) {
            // build the key from given TimeEntry
            final Date entryDate = e.getStart();
            final Calendar cal = Calendar.getInstance();
            cal.setTime(entryDate);
            // reset to midnight
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // check if date is already set as key previously
            final Long key = cal.getTimeInMillis();
            if (!perDay.containsKey(key)) {
                perDay.put(key, new TimeSumWithDate(cal.getTime(), 0L));
            }
            // add sum to existing entry
            final TimeSumWithDate sum = perDay.get(key);
            sum.addDuration(new TimeSum(e).getDuration());
        }

        final List<TimeSumWithDate> sortedPerDay = Arrays.asList(perDay.values().toArray(new TimeSumWithDate[0]));
        Collections.sort(sortedPerDay, new Comparator<TimeSumWithDate>() {
            @Override
            public int compare(TimeSumWithDate o1, TimeSumWithDate o2) {
                // sort DESC by start date
                return o2.getDate().compareTo(o1.getDate());
            }
        });
        return sortedPerDay;
    }

    public List<TimeSumWithDate> calculateWeeklyTimeSum(final List<TimeEntry> allEntries) {
        final Map<Long, TimeSumWithDate> perWeek = new HashMap<>();

        for (TimeEntry e : allEntries) {
            // build the key from given TimeEntry
            final Date entryDate = e.getStart();
            LOGGER.info("Got entry at " + entryDate);
            final Calendar cal = Calendar.getInstance();
            cal.setTime(entryDate);
            // reset to first of week
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            // reset to midnight
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // check if date is already set as key previously
            final Long key = cal.getTimeInMillis();
            if (!perWeek.containsKey(key)) {
                perWeek.put(key, new TimeSumWithDate(cal.getTime(), 0L));
            }
            // add sum to existing entry
            final TimeSumWithDate sum = perWeek.get(key);
            final TimeSum timeSum = new TimeSum(e);
            sum.addDuration(timeSum.getDuration());
            LOGGER.info("Add timesum " + timeSum + " to " + sum.getDate() + " with duration " + sum.getReadableDuration());
        }

        final List<TimeSumWithDate> sortedPerWeek = Arrays.asList(perWeek.values().toArray(new TimeSumWithDate[0]));
        Collections.sort(sortedPerWeek, new Comparator<TimeSumWithDate>() {
            @Override
            public int compare(TimeSumWithDate o1, TimeSumWithDate o2) {
                // sort DESC by start date
                return o2.getDate().compareTo(o1.getDate());
            }
        });
        return sortedPerWeek;
    }

    public List<TimeSumWithDate> calculateMonthlyTimeSum(final List<TimeEntry> allEntries) {
        final Map<Long, TimeSumWithDate> perMonth = new HashMap<>();

        for (TimeEntry e : allEntries) {
            // build the key from given TimeEntry
            final Date entryDate = e.getStart();
            final Calendar cal = Calendar.getInstance();
            cal.setTime(entryDate);
            // reset to first of month
            cal.set(Calendar.DAY_OF_MONTH, 1);
            // reset to midnight
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // check if date is already set as key previously
            final Long key = cal.getTimeInMillis();
            if (!perMonth.containsKey(key)) {
                perMonth.put(key, new TimeSumWithDate(cal.getTime(), 0L));
            }
            // add sum to existing entry
            final TimeSumWithDate sum = perMonth.get(key);
            sum.addDuration(new TimeSum(e).getDuration());
        }

        final List<TimeSumWithDate> sortedPerMonth = Arrays.asList(perMonth.values().toArray(new TimeSumWithDate[0]));
        Collections.sort(sortedPerMonth, new Comparator<TimeSumWithDate>() {
            @Override
            public int compare(TimeSumWithDate o1, TimeSumWithDate o2) {
                // sort DESC by start date
                return o2.getDate().compareTo(o1.getDate());
            }
        });
        return sortedPerMonth;
    }
}