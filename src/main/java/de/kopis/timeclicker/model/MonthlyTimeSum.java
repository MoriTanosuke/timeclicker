package de.kopis.timeclicker.model;

import de.kopis.timeclicker.Application;
import de.kopis.timeclicker.utils.WorkdayCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

public class MonthlyTimeSum extends TimeSum {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyTimeSum.class);

    private final Date firstOfMonth;
    private final Date lastOfMonth;
    private final long expectedDuration;

    public MonthlyTimeSum(Date month, long duration) {
        super(duration);
        this.firstOfMonth = makeFirstOfMonth(month);
        lastOfMonth = makeLastOfMonth(month);

        expectedDuration = WorkdayCalculator.getWorkingDays(this.firstOfMonth, lastOfMonth) * Application.HOURS_PER_DAY_IN_MILLISECONDS;
        LOGGER.debug("Setting expected duration for " + this.firstOfMonth + " to " + expectedDuration);
    }

    public static Date makeLastOfMonth(Date month) {
        final Calendar thisMonth = Calendar.getInstance();
        thisMonth.setTime(month);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        while (cal.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH)) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        // roll back one day, we counted 1 too far
        cal.add(Calendar.DAY_OF_MONTH, -1);
        LOGGER.debug("Last day of firstOfMonth: " + cal.get(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static Date makeFirstOfMonth(Date d) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        // reset to first of firstOfMonth
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // reset to midnight
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public Date getDate() {
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
        first.setTime(firstOfMonth);
        final Calendar last = Calendar.getInstance();
        last.setTime(lastOfMonth);
        final Calendar current = Calendar.getInstance();
        // check only on start date
        current.setTime(entry.getStart());
        if (current.before(first)) {
            throw new IllegalArgumentException(entry + " started before " + first);
        }
        if (current.after(last)) {
            throw new IllegalArgumentException(entry + " started after " + last);
        }
    }
}
