package de.kopis.timeclicker.model;

import java.io.Serializable;
import java.util.Date;

public class TimeSum implements Serializable {
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

    private long calculateDuration(TimeEntry entry) {
        final Date start = entry.getStart();
        // check if the entity is already stopped, else use the current date
        Date stop = entry.getStop();
        if (stop == null) {
            stop = new Date();
        }

        return stop.getTime() - start.getTime();
    }

    @Override
    public String toString() {
        return "TimeSum{" +
                "duration=" + duration +
                '}';
    }
}
