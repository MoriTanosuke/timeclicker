package de.kopis.timeclicker.model;

import de.kopis.timeclicker.utils.DurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

public class TimeSum implements Serializable, Comparable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSum.class);

    private static final long serialVersionUID = 1L;
    private long duration;

    public TimeSum(long duration) {
        this.duration = duration;
    }

    public TimeSum(TimeEntry entry) {
        this.duration = calculateDuration(entry);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void addDuration(long duration) {
        this.duration += duration;
    }

    public void add(final TimeSum sum) {
        this.duration += sum.getDuration();
    }

    private long calculateDuration(TimeEntry entry) {
        final Date start = entry.getStart();
        // check if the entity is already stopped, else use the current date
        Date stop = entry.getStop();
        if (stop == null) {
            stop = new Date();
        }

        long duration = stop.getTime() - start.getTime() - entry.getBreakDuration();
        LOGGER.debug("Calculated duration: " + duration);
        return duration;
    }

    @Override
    public String toString() {
        return getReadableDuration(this);
    }

    public String getReadableDuration() {
        return getReadableDuration(this);
    }

    private String getReadableDuration(TimeSum sum) {
        if (sum != null) {
            return DurationUtils.getReadableDuration(sum.getDuration());
        } else {
            return "--";
        }
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof TimeSum)) return -1;

        TimeSum other = (TimeSum) o;
        return (int) (other.duration -= duration);
    }
}
